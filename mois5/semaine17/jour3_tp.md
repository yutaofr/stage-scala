# TP Jour 3 : La Vision Rayons-X

**Durée :** ~4h | **Fil Rouge :** Instrumentation OpenTelemetry & Jaeger

---

## Exercice 1 : Setup Jaeger (1h)

1. Ajoute le service `jaeger` à ton `docker-compose.yml`.
2. Vérifie que l'interface est accessible sur `http://localhost:16686`.

---

## Exercice 2 : Instrumentation OTel (1h30)

1. Ajoute les dépendances `zio-telemetry`.
2. Configure ton application pour envoyer les spans à l'agent OTel.
3. Ajoute une span manuelle autour de ta boucle de netting : `ZIO.logSpan("netting-process") { ... }`.

---

## Exercice 3 : Propagation Kafka (1h30)

1. Modifie ton `Simulator` pour injecter un `TraceID` dans les headers Kafka.
2. Modifie ton `ClearingEngine` pour extraire ce `TraceID` et l'utiliser comme contexte parent.
3. Envoie une transaction et observe dans Jaeger le lien direct entre le simulateur et le moteur de clearing.

**Livrable** : Code source avec headers Kafka personnalisés et capture d'écran d'une trace complète dans Jaeger.
