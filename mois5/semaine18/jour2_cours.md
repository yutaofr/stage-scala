---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 2 — Chaos Engineering"
---

# Chaos Engineering
## Apprendre à aimer les pannes

**Durée :** ~2h | **Fil Rouge :** "Et si on coupait Kafka maintenant ?"

---

# 📋 Objectifs du Jour

- Comprendre la philosophie du Chaos Engineering (L'héritage de Netflix).
- Apprendre à tester la résilience **réellement**.
- Injecter des fautes : Réseau lent, crash de serveur, disque plein.
- Vérifier que le système se comporte comme prévu (grâce à la supervision S13 et ZIO S14).

---

# 1. Pourquoi détruire son propre système ?

Dans un système distribué, tout ce qui peut échouer échouera (Loi de Murphy).
Le Chaos Engineering consiste à injecter des pannes de manière contrôlée pour vérifier que nos mécanismes de survie (Retries, Circuit Breakers, Supervision) fonctionnent.

---

# 2. La Méthodologie

1. **Hypothèse** : "Si je coupe 1 broker Kafka sur 3, le système continue de tourner".
2. **Mesure** : Regarder le dashboard Grafana (Tout est vert).
3. **Injection** : `docker-compose stop kafka-1`.
4. **Analyse** : Est-ce que le système a tenu ? Y a-t-il eu une perte de données ?

---

# 3. Toxiproxy & Gremlin

Il existe des outils pour simuler des pannes complexes :
- **Toxiproxy** : Simule un réseau lent ou qui coupe de temps en temps.
- **Chaos Monkey** : Arrête des instances au hasard.

---

# 🏗️ Application : Le Test de Survie d'ATH

Nous allons jouer les "méchants". Pendant que le simulateur envoie des virement, nous allons éteindre Cassandra. Nous devrons observer que le moteur ZIO retente indéfiniment sans rien perdre, puis repart proprement dès que Cassandra revient.

---

# 🧠 Quiz Rapide

1. Qu'est-ce que le "Chaos Monkey" ? (Un script qui éteint des serveurs au hasard pour tester la résilience).
2. Doit-on faire du Chaos Engineering avant d'avoir du monitoring ? (Non, sans monitoring, on ne sait pas ce qu'on a cassé).
3. Pourquoi tester la coupure réseau entre l'app et la base ? (Car c'est la panne la plus courante et la plus difficile à gérer).

---

# 📝 Résumé du Jour

- La fiabilité n'est pas un accident, c'est une conception validée par le chaos.
- Injecter des erreurs permet d'avoir confiance dans son code de résilience.
- Un système robuste est un système qui ne craint pas la panne.

**Prochaine étape** : Injecter le chaos dans le TP 87 !
