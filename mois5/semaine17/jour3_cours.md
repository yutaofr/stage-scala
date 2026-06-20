---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 17"
footer: "Jour 3 — Tracing Distribué (OpenTelemetry)"
---

# Tracing Distribué
## Suivre un message dans le labyrinthe des services

**Durée :** ~2h | **Fil Rouge :** Le voyage d'une transaction (Simulator -> Kafka -> App)

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Span** et de **Trace ID**.
- Découvrir la norme **OpenTelemetry** (OTel).
- Apprendre à propager le contexte de trace à travers Kafka.
- Visualiser le trajet d'une transaction dans l'outil **Jaeger**.

---

# 1. Pourquoi le Tracing ?

Dans un système distribué, une erreur peut venir de n'importe quel service.
- Le log dit : "Erreur de validation".
- La trace dit : "C'est parce que le `BankService` a répondu en 5 secondes, provoquant un timeout dans le `ClearingEngine`".

---

# 2. Anatomie d'une Trace

- **Trace ID** : Un identifiant unique pour toute l'opération (de bout en bout).
- **Span** : Une étape spécifique (ex: "Parsing CSV", "Saving to Cassandra").
- **Parent/Child** : les spans sont organisés en hiérarchie causale.

---

# 3. Propaguer via Kafka

Kafka permet d'ajouter des headers. Un propagateur injecte le contexte W3C, notamment `traceparent`, puis le consumer l'extrait avant de créer son span enfant. Un Trace ID seul ne contient ni Span ID, ni flags, ni format de propagation.

> [!TIP]
> ZIO Telemetry facilite la création des spans. La propagation Kafka doit néanmoins être configurée ou instrumentée explicitement.

---

# 🏗️ Application : La Vision Rayons-X

Nous allons instrumenter notre pipeline complet. Chaque transaction générée créera une trace. Nous pourrons voir dans une interface web (Jaeger) chaque milliseconde passée dans Kafka, puis dans la validation Scala, puis dans l'écriture Cassandra.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre un log et une trace ? (Le log est un point isolé, la trace relie tous les points d'une même action).
2. Où stocke-t-on l'identifiant de trace quand on envoie un message Kafka ? (Dans les Headers du message).
3. À quoi sert l'outil Jaeger ? (À visualiser les traces sous forme de graphes temporels).

---

# 📝 Résumé du Jour

- Le tracing est le juge de paix des systèmes distribués.
- OpenTelemetry est le standard pour collecter ces données sans s'enfermer chez un vendeur.
- Tu peux maintenant prouver exactement où se trouve un ralentissement dans ton système.
- Ton application est "Transparente".

**Prochaine étape** : Utiliser le Kit 17.3 dans le TP du Jour 3.
