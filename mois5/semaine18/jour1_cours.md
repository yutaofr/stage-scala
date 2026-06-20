---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 1 — Tests de Charge (Gatling)"
---

# Tests de Performance
## Pousser le moteur dans ses retranchements

**Durée :** ~2h | **Fil Rouge :** Scénario : 100 000 transactions / minute

---

# 📋 Objectifs du Jour

- Comprendre l'importance de la validation de performance.
- Découvrir l'outil **Gatling** (DSL Scala).
- Apprendre à simuler des milliers d'utilisateurs/banques.
- Identifier le point de rupture (Breaking Point) du système.

---

# 1. Pourquoi tester la charge ?

Un code qui fonctionne pour 10 transactions peut s'effondrer sous 1 million.
- Latence qui explose.
- Mémoire saturée (OutOfMemoryError).
- Goulot d'étranglement sur Kafka ou Cassandra.

---

# 2. Gatling : La Charge par le Code

Gatling utilise Scala pour définir des scénarios de test.

```scala
val scn = scenario("Clearing Stress Test")
  .exec(http("Send Transaction")
    .post("/ingest")
    .body(StringBody("""{"id": "tx123", ...}""")).asJson)
  .pause(1)

setUp(
  scn.inject(atOnceUsers(1000))
).protocols(httpProtocol)
```

> 💡 On peut simuler une "rampe" (montée en charge progressive).

---

# 3. Analyser le rapport Gatling

Gatling génère un rapport HTML avec :
- Le nombre de requêtes par seconde (RPS).
- Les temps de réponse (min, max, p95, p99).
- Le taux d'erreur.

---

# 🏗️ Application : Le Test de Stress ATH

Nous allons cibler un adaptateur HTTP de laboratoire qui publie les requêtes dans Kafka. L'objectif est d'identifier le premier SLO dépassé et le composant saturé.

---

# 🧠 Quiz Rapide

1. Gatling est-il écrit en Scala ? (Oui).
2. Que représente la p99 ? (99 % des observations sont inférieures ou égales à cette valeur ; ce n'est pas le maximum).
3. Pourquoi injecter des utilisateurs progressivement plutôt que tous d'un coup ? (Pour simuler la réalité d'une journée de travail et observer comment le système s'adapte).

---

# 📝 Résumé du Jour

- On ne devine pas la performance, on la mesure.
- Gatling est le standard pour les tests de charge en environnement JVM.
- Ton application doit tenir les pics d'activité bancaire du matin.
- Plus tu connais tes limites, plus tu es prêt pour la production.

**Prochaine étape** : Utiliser le Kit 18.1 dans le TP du Jour 1.
