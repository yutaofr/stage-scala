# Starter Kit Semaine 17 : Observabilité

Le kit ajoute les signaux d'observabilité sans mélanger leur transport avec la logique de clearing.

## Kit 17.1 — Contexte de log Fiber-safe

**Fichier fourni :** `fil-rouge/src/main/scala/observability/ObservedProcessing.scala`

```scala
package observability

import zio.*
import zio.metrics.*
import distributed.kafka.RecordEnvelope

object ObservedProcessing:
  def withTransactionContext[R, E, A](
    txId: String,
    record: RecordEnvelope
  )(effect: ZIO[R, E, A]): ZIO[R, E, A] =
    ZIO.logAnnotate("txId", txId) {
      ZIO.logAnnotate("topic", record.topic) {
        ZIO.logAnnotate("partition", record.partition.toString) {
          ZIO.logAnnotate("offset", record.offset.toString)(effect)
        }
      }
    }

  def process[R, E, A](
    txId: String,
    record: RecordEnvelope
  )(durableProcessing: ZIO[R, E, A]): ZIO[R, E, A] =
    withTransactionContext(txId, record) {
      ZIO.logInfo("transaction.started") *>
        durableProcessing.tapBoth(
          error => ZIO.logError(s"transaction.failed: $error"),
          _ => ZIO.logInfo("transaction.completed")
        )
    }
```

**Critère :** deux Fibers concurrentes gardent des annotations distinctes.

## Kit 17.2 — Métriques du pipeline

```scala
package observability

import zio.*

object ClearingMetrics:
  val processed = Metric.counter("clearing_transactions_processed_total")
  val duration  = Metric.histogram(
    "clearing_processing_duration_seconds",
    MetricKeyType.Histogram.Boundaries.linear(0.0, 0.1, 25)
  )

  def observe[R, E, A](effect: ZIO[R, E, A]): ZIO[R, E, A] =
    for
      start <- Clock.nanoTime
      result <- effect
        .tapBoth(
          _ => processed.tagged("status", "failure").increment,
          _ => processed.tagged("status", "success").increment
        )
        .ensuring(
          Clock.nanoTime.flatMap { end =>
            duration.update((end - start).toDouble / 1_000_000_000d)
          }
        )
    yield result
```

`ensuring` mesure aussi les échecs et les interruptions. Ajuste ensuite les buckets à partir des mesures réelles ; la version initiale couvre 0 à 2,5 secondes par pas de 100 ms.

## Kit 17.3 — Stack OpenTelemetry

**Extrait Compose :**

```yaml
services:
  otel-collector:
    image: otel/opentelemetry-collector-contrib
    command: ["--config=/etc/otelcol/config.yml"]
    volumes:
      - ./otel-collector.yml:/etc/otelcol/config.yml:ro
    ports:
      - "4317:4317"

  jaeger:
    image: jaegertracing/all-in-one
    ports:
      - "16686:16686"
      - "4317"
```

**Algorithme de propagation Kafka :**

```scala
// Producer : propagator.inject(currentContext, kafkaHeadersCarrier)
// Consumer : val parent = propagator.extract(Context.root(), kafkaHeadersCarrier)
//            tracer.span("clearing.consume", parent) { process(record) }
```

**TODO stagiaire :** implémenter le carrier Kafka et vérifier la présence de `traceparent`.

## Kit 17.4 — Provisioning Prometheus/Grafana

```yaml
services:
  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml:ro
    ports: ["9090:9090"]

  grafana:
    image: grafana/grafana
    volumes:
      - ./grafana/provisioning:/etc/grafana/provisioning:ro
      - ./grafana/dashboards:/var/lib/grafana/dashboards:ro
    ports: ["3000:3000"]
```

```yaml
# prometheus.yml
scrape_configs:
  - job_name: clearing-engine
    static_configs:
      - targets: ["engine:8080"]
```

## Kit 17.5 — Alertes vérifiables

```yaml
groups:
  - name: clearing
    rules:
      - alert: EngineDown
        expr: up{job="clearing-engine"} == 0
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "Clearing Engine indisponible"
          runbook: "Vérifier le conteneur, les logs et la connectivité réseau."

      - alert: HighErrorRate
        expr: |
          (
            sum(rate(clearing_transactions_processed_total{status="failure"}[5m]))
            /
            clamp_min(sum(rate(clearing_transactions_processed_total[5m])), 0.001)
          ) > 0.10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Taux d'erreur supérieur au seuil du laboratoire"
```

**Commandes de contrôle :**

```bash
promtool check config prometheus.yml
promtool check rules alert_rules.yml
docker compose config
```
