#!/usr/bin/env bash
set -euo pipefail

ROOT=$(cd "$(dirname "$0")/.." && pwd)
cd "$ROOT"

COMPOSE_FILE="$ROOT/docker/docker-compose-v32.yml"
COMPOSE=(docker compose -f "$COMPOSE_FILE")
ARTIFACTS="$ROOT/target/v32-runtime"
APP_LOG="$ARTIFACTS/application.log"
JSON_LOG="$ARTIFACTS/application-json.log"
TRACES="$ARTIFACTS/jaeger-traces.json"
INPUT="$ARTIFACTS/clearing-input.jsonl"
OUTPUT="$ARTIFACTS/clearing-output.jsonl"
DLQ="$ARTIFACTS/clearing-dlq.jsonl"
CASSANDRA_DUMP="$ARTIFACTS/cassandra.txt"
WEBHOOK_DUMP="$ARTIFACTS/notifications.jsonl"
SENSITIVE_VALUES="$ARTIFACTS/sensitive-values.txt"
EXPECTED_RECORDS=500
EXPECTED_OUTPUT_RECORDS=485
EXPECTED_DLQ_RECORDS=5
EXPECTED_SENSITIVE_VALUES=$((EXPECTED_OUTPUT_RECORDS * 2))
APP_PID=""
KEEP_STACK=${KEEP_STACK:-0}
RUNTIME_KEEP_STACK=0

cleanup() {
  if [[ "$RUNTIME_KEEP_STACK" == "1" ]]; then
    echo "V32_RUNTIME_KEEP_STACK app_pid=$APP_PID grafana=http://localhost:3000 jaeger=http://localhost:16686"
    return
  fi
  if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill -TERM "$APP_PID" 2>/dev/null || true
    wait "$APP_PID" 2>/dev/null || true
  fi
  "${COMPOSE[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

wait_for() {
  local label=$1
  shift
  local attempts=${WAIT_ATTEMPTS:-120}
  for ((attempt = 1; attempt <= attempts; attempt += 1)); do
    if "$@" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  echo "timeout waiting for $label" >&2
  return 1
}

url_ok() {
  curl --fail --silent --show-error "$1" >/dev/null
}

prom_query() {
  curl --fail --silent --get "http://localhost:9090/api/v1/query" \
    --data-urlencode "query=$1"
}

prom_value() {
  prom_query "$1" | jq -r '.data.result[0].value[1] // "missing"'
}

target_health() {
  local expected=$1
  curl --fail --silent "http://localhost:9090/api/v1/targets" |
    jq -e --arg health "$expected" '
      .data.activeTargets[] |
      select(.labels.job == "clearing-engine" and .health == $health)
    '
}

prom_alert_state() {
  local expected=$1
  curl --fail --silent "http://localhost:9090/api/v1/alerts" |
    jq -e --arg state "$expected" '
      .data.alerts[] |
      select(.labels.alertname == "EngineDown" and .state == $state)
    '
}

prom_alert_absent() {
  curl --fail --silent "http://localhost:9090/api/v1/alerts" |
    jq -e '[.data.alerts[] | select(.labels.alertname == "EngineDown")] | length == 0'
}

alertmanager_firing() {
  curl --fail --silent "http://localhost:9093/api/v2/alerts" |
    jq -e '.[] | select(.labels.alertname == "EngineDown" and .status.state == "active")'
}

alertmanager_absent() {
  curl --fail --silent "http://localhost:9093/api/v2/alerts" |
    jq -e '[.[] | select(.labels.alertname == "EngineDown")] | length == 0'
}

alertmanager_start() {
  curl --fail --silent "http://localhost:9093/api/v2/alerts" |
    jq -r '.[] | select(.labels.alertname == "EngineDown" and .status.state == "active") | .startsAt' |
    head -n 1
}

webhook_count() {
  "${COMPOSE[@]}" exec -T webhook sh -c \
    'if test -f /data/notifications.jsonl; then wc -l < /data/notifications.jsonl; else echo 0; fi' |
    tr -d '[:space:]'
}

webhook_status_after() {
  local expected=$1
  local baseline=$2
  local starts_at=$3
  "${COMPOSE[@]}" exec -T webhook sh -c \
    'test -s /data/notifications.jsonl && cat /data/notifications.jsonl' |
    jq -s -e --arg status "$expected" --arg startsAt "$starts_at" \
      --argjson baseline "$baseline" \
      '.[$baseline:] | any(
        .payload.status == $status and
        any(.payload.alerts[]; .startsAt == $startsAt)
      )'
}

dump_topic() {
  local topic=$1
  local expected=$2
  local destination=$3
  "${COMPOSE[@]}" exec -T kafka \
    /opt/kafka/bin/kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 --topic "$topic" \
    --from-beginning --timeout-ms 10000 >"$destination" 2>/dev/null || true
  local actual
  actual=$(wc -l <"$destination" | tr -d '[:space:]')
  if [[ "$actual" -ne "$expected" ]]; then
    echo "topic $topic: $actual records, expected $expected" >&2
    return 1
  fi
}

metrics_reconciled() {
  local total
  local lag
  total=$(prom_value 'sum(clearing_transactions_processed_total)')
  lag=$(prom_value 'sum(clearing_kafka_lag_records)')
  jq -en --arg total "$total" --arg lag "$lag" \
    --argjson expected "$EXPECTED_RECORDS" \
    '($total | tonumber | floor) == $expected and ($lag | tonumber) == 0'
}

start_app() {
  sbt -batch -Dsbt.server.autostart=false \
    -Dlogback.configurationFile="$ROOT/src/main/resources/logback-json.xml" \
    'set Compile / run / fork := false' \
    'run consumer --group-id marc-v32-runtime' >>"$APP_LOG" 2>&1 &
  APP_PID=$!
  wait_for "Clearing Engine health" url_ok "http://localhost:8080/health"
}

stop_app() {
  if [[ -n "$APP_PID" ]] && kill -0 "$APP_PID" 2>/dev/null; then
    kill -TERM "$APP_PID"
    for _ in $(seq 1 30); do
      if ! kill -0 "$APP_PID" 2>/dev/null; then
        break
      fi
      sleep 1
    done
    if kill -0 "$APP_PID" 2>/dev/null; then
      kill -KILL "$APP_PID"
    fi
    wait "$APP_PID" 2>/dev/null || true
  fi
  APP_PID=""
}

rm -rf "$ARTIFACTS"
mkdir -p "$ARTIFACTS"
"${COMPOSE[@]}" down --volumes --remove-orphans >/dev/null 2>&1 || true
"${COMPOSE[@]}" up -d
"${COMPOSE[@]}" wait kafka-init cassandra-init

wait_for "Jaeger" url_ok "http://localhost:16686/"
wait_for "Collector" url_ok "http://localhost:13133/"
wait_for "Prometheus" url_ok "http://localhost:9090/-/healthy"
wait_for "Alertmanager" url_ok "http://localhost:9093/-/healthy"
wait_for "Grafana" url_ok "http://localhost:3000/api/health"
wait_for "webhook" url_ok "http://localhost:8081/health"

start_app
TARGET_UP_QUERY='health="up"'
wait_for "Prometheus target health=\"up\"" target_health up

sbt -batch -Dsbt.server.autostart=false 'run qualify --seed 1701'
wait_for "500 processed records and lag zero" metrics_reconciled

METRIC_TOTAL=$(prom_value 'sum(clearing_transactions_processed_total)' | jq -r 'tonumber | floor')
METRIC_LAG=$(prom_value 'sum(clearing_kafka_lag_records)' | jq -r 'tonumber')
[[ "$METRIC_TOTAL" -eq "$EXPECTED_RECORDS" ]]
[[ "$METRIC_LAG" == "0" ]]

curl --fail --silent "http://localhost:8080/metrics" |
  grep -q 'clearing_processing_duration_seconds_bucket'
curl --fail --silent "http://localhost:8080/metrics" |
  grep -q 'clearing_transactions_processed_total'
curl --fail --silent "http://localhost:8080/metrics" |
  grep -q 'clearing_kafka_lag_records'

wait_for "Jaeger clearing trace" bash -c \
  'curl --fail --silent "http://localhost:16686/api/traces?service=clearing-engine&limit=20" | jq -e '\''[.data[].spans[].operationName] | index("clearing.consume") != null'\'''
curl --fail --silent \
  "http://localhost:16686/api/traces?service=clearing-engine&limit=20" >"$TRACES"
for span in clearing.consume parse validate netting persist; do
  jq -e --arg span "$span" \
    '[.data[].spans[].operationName] | index($span) != null' "$TRACES" >/dev/null
done

curl --fail --silent \
  "http://localhost:3000/api/datasources/uid/prometheus-clearing" |
  jq -e '.uid == "prometheus-clearing" and .url == "http://prometheus:9090"' >/dev/null
curl --fail --silent \
  "http://localhost:3000/api/dashboards/uid/clearing-engine-v32" |
  jq -e '.dashboard.uid == "clearing-engine-v32" and (.dashboard.panels | length) == 5' >/dev/null

grep '^{' "$APP_LOG" >"$JSON_LOG"
test -s "$JSON_LOG"
while IFS= read -r line; do
  jq -e '
    .["@timestamp"] and .level and .message and
    .service == "clearing-engine" and .environment
  ' <<<"$line" >/dev/null
done <"$JSON_LOG"
jq -e 'select(.message == "clearing record completed status=success") | .txId and .topic and .partition and .offset' \
  "$JSON_LOG" >/dev/null

dump_topic clearing-input "$EXPECTED_RECORDS" "$INPUT"
dump_topic clearing-output "$EXPECTED_OUTPUT_RECORDS" "$OUTPUT"
dump_topic clearing-dlq "$EXPECTED_DLQ_RECORDS" "$DLQ"
"${COMPOSE[@]}" exec -T cassandra cqlsh localhost 9042 -e \
  'SELECT * FROM clearing.processing_state LIMIT 600; SELECT * FROM clearing.clearing_history_by_day LIMIT 600;' \
  >"$CASSANDRA_DUMP"

: >"$SENSITIVE_VALUES"
while IFS= read -r line; do
  jq -r \
    'objects | .sourceIban, .destinationIban | select(type == "string")' \
    <<<"$line" 2>/dev/null >>"$SENSITIVE_VALUES" || true
done <"$INPUT"
sort -u -o "$SENSITIVE_VALUES" "$SENSITIVE_VALUES"
SENSITIVE_VALUE_COUNT=$(wc -l <"$SENSITIVE_VALUES" | tr -d '[:space:]')
[[ "$SENSITIVE_VALUE_COUNT" -eq "$EXPECTED_SENSITIVE_VALUES" ]]

if grep -F -f "$SENSITIVE_VALUES" \
  "$APP_LOG" "$TRACES" "$OUTPUT" "$DLQ" "$CASSANDRA_DUMP"; then
  echo "sensitive data found" >&2
  exit 1
fi

wait_for "EngineDown clear before fault" prom_alert_absent
wait_for "Alertmanager clear before fault" alertmanager_absent
ALERT_BASELINE=$(webhook_count)
stop_app
TARGET_DOWN_QUERY='health="down"'
wait_for "Prometheus target health=\"down\"" target_health down
wait_for "EngineDown pending" prom_alert_state pending
wait_for "EngineDown firing" prom_alert_state firing
wait_for "Alertmanager firing" alertmanager_firing
ALERT_STARTS_AT=$(alertmanager_start)
test -n "$ALERT_STARTS_AT"
wait_for "webhook firing" webhook_status_after firing \
  "$ALERT_BASELINE" "$ALERT_STARTS_AT"
FIRING_COUNT=$(webhook_count)
[[ "$FIRING_COUNT" -gt "$ALERT_BASELINE" ]]

start_app
wait_for "Prometheus target recovered" target_health up
wait_for "EngineDown resolved in Prometheus" prom_alert_absent
wait_for "webhook resolved" webhook_status_after resolved \
  "$FIRING_COUNT" "$ALERT_STARTS_AT"
"${COMPOSE[@]}" exec -T webhook cat /data/notifications.jsonl >"$WEBHOOK_DUMP"
jq -s -e --arg startsAt "$ALERT_STARTS_AT" --argjson baseline "$ALERT_BASELINE" \
  '.[$baseline:] | any(
    .payload.status == "firing" and
    any(.payload.alerts[]; .startsAt == $startsAt)
  )' "$WEBHOOK_DUMP" >/dev/null
jq -s -e --arg startsAt "$ALERT_STARTS_AT" --argjson baseline "$FIRING_COUNT" \
  '.[$baseline:] | any(
    .payload.status == "resolved" and
    any(.payload.alerts[]; .startsAt == $startsAt)
  )' "$WEBHOOK_DUMP" >/dev/null
alertmanager_absent

if [[ "$KEEP_STACK" == "1" ]]; then
  RUNTIME_KEEP_STACK=1
else
  stop_app
fi
echo "V32_RUNTIME_OK records=$METRIC_TOTAL lag=$METRIC_LAG pending firing resolved"
