# Starter Kit Semaine 17 : Observabilité

Le kit fournit un projet autonome et auto-contenu sous le dossier `starter_kit/`. Il permet d'ajouter les signaux d'observabilité sans mélanger leur transport avec la logique de clearing.

## Préflight

```bash
cd mois5/semaine17/starter_kit
sbt test
```

---

## Kit 17.1 — Contexte de log thread-safe

**Fichier fourni :** `mois5/semaine17/starter_kit/src/main/scala/observability/ObservedProcessing.scala`

```scala
package observability

import distributed.kafka.RecordEnvelope
import org.slf4j.MDC

object ObservedProcessing:
  def withTransactionContext[A](
    txId: String,
    record: RecordEnvelope
  )(block: => A): A =
    // TODO : Mettre en œuvre MDC
    ???

  def process[A](
    txId: String,
    record: RecordEnvelope
  )(durableProcessing: => A): A =
    // TODO : Logger le début, le succès et l'échec de la transaction
    ???
```

**Critère :** deux threads concurrents (ou threads virtuels) gardent des annotations distinctes grâce à la ThreadLocal de MDC.

---

## Kit 17.2 — Métriques du pipeline

**Fichier fourni :** `mois5/semaine17/starter_kit/src/main/scala/observability/ClearingMetrics.scala`

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
    // TODO : implémenter la mesure de temps et l'incrémentation des compteurs
    ???
```

`timer` mesure aussi les échecs et les interruptions. Ajuste ensuite les buckets de votre exportateur Prometheus à partir des mesures réelles.

---

## Kit 17.3 — Stack OpenTelemetry

**Extrait Compose :** `mois5/semaine17/starter_kit/docker/docker-compose.yml`

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
// Fichier : mois5/semaine17/starter_kit/src/main/scala/observability/OpenTelemetrySetup.scala
// Producer : propagator.inject(currentContext, kafkaHeadersCarrier)
// Consumer : val parent = propagator.extract(Context.root(), kafkaHeadersCarrier)
//            tracer.span("clearing.consume", parent) { process(record) }
```

**TODO stagiaire :** implémenter le carrier Kafka et vérifier la présence de `traceparent`.

---

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
    ports: ["3000:3000"]
```

```yaml
# prometheus.yml
scrape_configs:
  - job_name: clearing-engine
    static_configs:
      - targets: ["host.docker.internal:8080"]
```

---

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
cd mois5/semaine17/starter_kit/docker
promtool check config prometheus.yml
docker compose config
```
