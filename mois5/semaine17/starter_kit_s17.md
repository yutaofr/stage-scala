# Starter Kit Semaine 17 : Observabilité

Le kit ajoute les signaux d'observabilité sans mélanger leur transport avec la logique de clearing.

## Kit 17.1 — Contexte de log thread-safe

**Fichier fourni :** `fil-rouge/src/main/scala/observability/ObservedProcessing.scala`

```scala
package observability

import distributed.kafka.RecordEnvelope
import org.slf4j.MDC

object ObservedProcessing:
  def withTransactionContext[A](
    txId: String,
    record: RecordEnvelope
  )(block: => A): A =
    MDC.put("txId", txId)
    MDC.put("topic", record.topic)
    MDC.put("partition", record.partition.toString)
    MDC.put("offset", record.offset.toString)
    try block
    finally MDC.clear()

  def process[A](
    txId: String,
    record: RecordEnvelope
  )(durableProcessing: => A): A =
    withTransactionContext(txId, record) {
      val logger = org.slf4j.LoggerFactory.getLogger("ObservedProcessing")
      logger.info("transaction.started")
      try {
        val res = durableProcessing
        logger.info("transaction.completed")
        res
      } catch {
        case error: Throwable =>
          logger.error(s"transaction.failed: ${error.getMessage}")
          throw error
      }
    }
```

**Critère :** deux threads concurrents (ou threads virtuels) gardent des annotations distinctes grâce à la ThreadLocal de MDC.

## Kit 17.2 — Métriques du pipeline

```scala
package observability

import io.micrometer.core.instrument.{Metrics, Timer}

object ClearingMetrics:
  val processedSuccess = Metrics.counter("clearing_transactions_processed_total", "status", "success")
  val processedFailure = Metrics.counter("clearing_transactions_processed_total", "status", "failure")
  val timer = Timer.builder("clearing_processing_duration_seconds")
    .description("Temps passé dans la logique de netting")
    .register(Metrics.globalRegistry)

  def observe[A](block: => A): A =
    val sample = Timer.start(Metrics.globalRegistry)
    try {
      val result = block
      processedSuccess.increment()
      result
    } catch {
      case error: Throwable =>
        processedFailure.increment()
        throw error
    } finally {
      sample.stop(timer)
    }
```

`timer` mesure aussi les échecs et les interruptions. Ajuste ensuite les buckets de votre exportateur Prometheus à partir des mesures réelles.

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
