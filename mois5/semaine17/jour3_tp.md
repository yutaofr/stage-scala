# TP Jour 3 : La Vision Rayons-X

**Durée :** ~4h | **Fil Rouge :** Instrumentation OpenTelemetry & Jaeger

---

## Point de départ

- Copie le **Kit 17.3** de `starter_kit_s17.md`.
- Envoie les données via OTLP vers un Collector, puis vers Jaeger.
- N'écris pas toi-même un Trace ID arbitraire.

## Exercice 1 : Setup Jaeger (1h)

1. Lance Jaeger et l'OpenTelemetry Collector fournis.
2. Vérifie l'interface Jaeger.
3. Vérifie les logs du Collector avant d'instrumenter l'application.

**Validation :** les deux services sont sains et le port OTLP est accessible.

---

## Exercice 2 : Instrumentation OTel (1h30)

1. Ajoute ZIO OpenTelemetry et l'exporter OTLP.
2. Configure le service name `clearing-engine`.
3. Crée des spans `parse`, `validate`, `netting` et `persist`.
4. Ajoute `tx.id` comme attribut de span, jamais comme nom de span.

**Validation :** Jaeger affiche une trace avec les quatre spans et leurs durées.

---

## Exercice 3 : Propagation Kafka (1h30)

1. Injecte le contexte courant avec un propagateur W3C dans les headers Kafka.
2. Extrait ce contexte côté consumer.
3. Crée le span consumer avec le contexte extrait comme parent.
4. Envoie aussi un message sans contexte et vérifie qu'une nouvelle trace démarre.

**Validation :** producer et consumer partagent la même trace, avec des Span IDs distincts.

**Livrable** : Code source avec headers Kafka personnalisés et capture d'écran d'une trace complète dans Jaeger.
