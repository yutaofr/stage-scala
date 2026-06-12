---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 5 — High Availability (Haute Disponibilité)"
---

# High Availability
## Ne jamais dépendre d'un seul serveur

**Durée :** ~2h | **Fil Rouge :** Scaler le moteur de clearing horizontalement

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Scalabilité Horizontale**.
- Apprendre à répartir la charge entre plusieurs instances.
- Maîtriser le rééquilibrage automatique de Kafka (**Rebalancing**).
- Simuler une infrastructure à "Zero Downtime".

---

# 1. Pourquoi la HA ?

Un serveur peut brûler, une zone réseau peut tomber.
- Si vous n'avez qu'un seul moteur de clearing, ATH s'arrête.
- Si vous avez 3 moteurs sur 3 serveurs différents, ATH continue.

---

# 2. Kafka et le Parallélisme

Grâce aux **Partitions** (M4-S15), Kafka sait comment distribuer le travail.
- 3 Partitions = 3 Moteurs peuvent travailler en même temps.
- Si le moteur 1 meurt, Kafka donne sa partition au moteur 2 et 3 automatiquement. C'est le **Rebalance**.

---

# 3. Stateless vs Stateful

Pour scaler horizontalement, une application doit être **Stateless** (Sans état local).
- Si le moteur stocke les transactions dans une variable locale, le moteur 2 ne les voit pas. ❌
- Si le moteur stocke tout dans Cassandra, n'importe quel moteur peut reprendre le travail. ✅

---

# 🏗️ Application : Le Cluster de Clearing

Nous allons modifier notre `docker-compose.yml` pour lancer **3 instances** de notre moteur de clearing. Nous observerons Kafka répartir les messages entre les 3. Puis nous en "tuerons" une et nous verrons les deux autres absorber le trafic sans perdre une seule transaction.

---

# 🧠 Quiz Rapide

1. Puis-je avoir 10 instances de mon application pour un topic qui n'a que 2 partitions ? (Oui, mais 8 instances seront inutiles car elles n'auront rien à lire).
2. Qu'est-ce qu'un "Rebalance" ? (Le processus de Kafka pour redistribuer les partitions quand un membre du groupe part ou arrive).
3. Pourquoi l'idempotence est-elle encore plus importante en HA ? (Parce que pendant un rebalance, un message peut être envoyé brièvement aux deux instances).

---

# 📝 Résumé de la Semaine 18

- Tu as stressé ton code avec Gatling.
- Tu l'as cassé avec du Chaos Engineering.
- Tu l'as observé à la loupe avec VisualVM.
- Tu l'as optimisé avec le Tuning JVM.
- Tu l'as rendu immortel avec la Haute Disponibilité.
- Ton application est maintenant un roc.

**Prochaine étape** : Déployer ton cluster dans le TP 90 !
