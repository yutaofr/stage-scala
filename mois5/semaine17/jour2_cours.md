---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 17"
footer: "Jour 2 — Métriques Prometheus"
---

# Métriques
## Mesurer la santé de ton application en temps réel

**Durée :** ~2h | **Fil Rouge :** Compteur de transactions & Temps de réponse

---

# 📋 Objectifs du Jour

- Comprendre le modèle de données de **Prometheus** (Time Series).
- Apprendre les 4 types de métriques : Counter, Gauge, Histogram, Summary.
- Instrumenter son code Scala pour exposer ces chiffres.
- Lancer un serveur Prometheus pour collecter les données.

---

# 1. Qu'est-ce que Prometheus ?

C'est une base de données optimisée pour les chiffres qui changent au cours du temps.
- **Pull model** : Prometheus appelle votre application toutes les 15s pour lire les compteurs.

### Le format
`clearing_transactions_total{bank="CIH"} 1250`

---

# 2. Les 4 Types Clés

1. **Counter** : Une valeur qui ne fait que monter (ex: Nombre total de transactions).
2. **Gauge** : Une valeur qui peut monter et descendre (ex: Occupation CPU, nombre d'utilisateurs connectés).
3. **Histogram** : Pour mesurer des durées (ex: Temps de validation). Permet de calculer des centiles (p99).
4. **Summary** : Similaire à l'histogramme mais calculé côté client.

---

# 3. Instrumenter avec ZIO

ZIO possède une bibliothèque `zio-metrics-connectors` qui rend l'instrumentation très simple.

```scala
val txCounter = Metric.counter("transactions_total")
  .tagged(MetricLabel("app", "clearing-engine"))

// Dans le code
txCounter.increment
```

---

# 🏗️ Application : Le Tableau de Bord Industriel

Nous allons exposer un endpoint HTTP (`/metrics`) sur notre moteur de clearing. Prometheus viendra y lire les statistiques de succès/échec de nos traitements Kafka.

---

# 🧠 Quiz Rapide

1. Mon application doit-elle "pousser" les métriques vers Prometheus ? (Non, c'est Prometheus qui vient les chercher).
2. Quel type de métrique utiliser pour le solde actuel d'une banque ? (Une Gauge).
3. Pourquoi l'Histogramme est-il utile pour le temps de réponse ? (Pour savoir si 99% des transactions sont traitées en moins de 100ms, même si la moyenne est plus basse).

---

# 📝 Résumé du Jour

- Les métriques donnent une vue d'ensemble du système.
- On ne peut pas améliorer ce qu'on ne mesure pas.
- Prometheus est le standard absolu de l'industrie Cloud.
- Ton application devient "Observable".

**Prochaine étape** : Exposer tes métriques dans le TP 82 !
