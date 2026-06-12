---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 17"
footer: "Jour 4 — Dashboards Grafana"
---

# Grafana
## L'art de la visualisation de données

**Durée :** ~2h | **Fil Rouge :** Le cockpit du Clearing Engine

---

# 📋 Objectifs du Jour

- Installer et configurer **Grafana**.
- Apprendre à connecter Prometheus comme source de données.
- Créer des graphiques (Panels) pertinents pour le métier et la technique.
- Partager et exporter son dashboard.

---

# 1. Qu'est-ce que Grafana ?

Si Prometheus est le cerveau (données), Grafana est le visage (visualisation).
C'est l'outil qui sera utilisé par les équipes opérationnelles d'ATH pour surveiller l'état des virements au Maroc.

---

# 2. Anatomie d'un Dashboard

Un bon dashboard doit répondre à 3 questions en 3 secondes :
1. **Est-ce que ça marche ?** (Taux de succès/échec).
2. **Est-ce que ça va vite ?** (Latence, temps de réponse).
3. **Est-ce que ça tient ?** (CPU, RAM, usage disque).

---

# 3. Le Langage PromQL

Pour créer un graphique, on utilise des requêtes Prometheus.
- `sum(rate(clearing_transactions_total[1m]))` : Débit par minute.
- `histogram_quantile(0.99, sum(rate(duration_bucket[5m])) by (le))` : Latence p99.

---

# 🏗️ Application : Le Cockpit ATH

Nous allons créer un dashboard complet incluant :
- Un "Single Stat" pour le nombre de transactions traitées ce jour.
- Un "Graph" pour le débit temps réel.
- Une "Gauge" pour l'utilisation mémoire du moteur Scala.

---

# 🧠 Quiz Rapide

1. Grafana stocke-t-il les données ? (Non, il les lit depuis Prometheus).
2. Qu'est-ce qu'un "Panel" dans Grafana ? (Un graphique ou un indicateur individuel).
3. Quel est l'intérêt de partager son dashboard via un fichier JSON ? (Pour pouvoir le versionner avec Git et le reproduire partout).

---

# 📝 Résumé du Jour

- Un dashboard rend ton travail visible et crédible.
- La visualisation permet de détecter des anomalies qu'un humain ne verrait pas dans les logs.
- Tu as maintenant un système digne d'un grand centre d'opération bancaire.
- Demain, nous ferons en sorte que le système nous prévienne tout seul en cas de problème.

**Prochaine étape** : Créer ton dashboard dans le TP 84 !
