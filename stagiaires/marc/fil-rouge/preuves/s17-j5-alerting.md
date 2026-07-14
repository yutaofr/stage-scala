# S17 J5 — SLO, règles et routage d'alertes

## Réalisation

Le fichier `slo-v32.md` définit deux objectifs glissants sur 30 jours :
disponibilité de traitement à 99,5 % et latence à 99 % sous 500 ms. Chaque SLO
nomme son SLI, sa population, sa source Prometheus, sa cible et son budget
d'erreur. Les cas sans trafic, maintenance, données invalides et perte de
scrape sont explicités.

Prometheus charge trois règles versionnées :

- `EngineDown` après 30 secondes sans scrape;
- `ClearingHighFailureRate` au-dessus de 0,5 % pendant 2 minutes;
- `ClearingProcessingLatencyHigh` lorsque le p99 dépasse 500 ms pendant 2
  minutes.

Chaque règle porte une severity, un summary et une description. Le ratio
d'échec utilise `clamp_min` pour protéger le dénominateur.

Alertmanager route vers un webhook HTTP distinct du Clearing Engine avec
`send_resolved: true`. Le receiver Python utilise seulement la bibliothèque
standard et conserve chaque notification JSON dans
`/data/notifications.jsonl`, porté par un volume Docker.

## Preuves de contrat

```bash
sbt 'testOnly clearing.v32.AlertingContractSpec'
docker run --rm \
  --entrypoint /bin/promtool \
  -v "$PWD/docker/observability:/etc/prometheus:ro" \
  prom/prometheus:v3.12.0 \
  check rules /etc/prometheus/clearing-rules.yml
docker compose -f docker/docker-compose-v32.yml config --quiet
```

Le gate runtime final a d'abord attendu l'absence d'`EngineDown` dans Prometheus
et Alertmanager, puis enregistré le nombre initial de notifications. Il a arrêté le moteur, observé `EngineDown` dans les états
`pending` puis `firing`, retrouvé l'alerte active dans Alertmanager et la
notification `firing` dans le webhook. Après redémarrage, la cible est revenue
à `up`, l'alerte a disparu de Prometheus et Alertmanager, puis le webhook a
reçu `resolved`. Le gate exige que les deux notifications soient postérieures
au baseline et portent le même `startsAt`; une notification de démarrage ne
peut donc plus satisfaire la reprise volontaire. Le journal final contient
quatre notifications : un couple firing/resolved de démarrage et un couple
distinct firing/resolved pour la panne injectée.
