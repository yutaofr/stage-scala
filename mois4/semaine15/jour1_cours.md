---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 15"
footer: "Jour 1 — Introduction à Kafka"
---

# Apache Kafka
## Le système nerveux du Clearing Temps Réel

**Durée :** ~2h | **Fil Rouge :** Architecture orientée messages

---

# 📋 Objectifs du Jour

- Comprendre le rôle de Kafka dans les architectures modernes.
- Maîtriser les concepts de base : **Topic**, **Partition**, **Segment**.
- Découvrir le rôle du **Producer** et du **Consumer**.
- Comprendre la notion d'**Offset** (Le marque-page du flux).

---

# 1. Pourquoi Kafka ?

Dans le clearing batch (S1 à S14), on attend que le fichier arrive. C'est lent.
Avec Kafka, chaque transaction est envoyée dès qu'elle arrive. Le clearing se fait **au fil de l'eau**.

### Avantages
- **Scalabilité** : Kafka peut absorber des millions de messages par seconde.
- **Persistance** : Si le moteur de clearing s'arrête, il reprend là où il en était (grâce aux Offsets).
- **Découplage** : La banque envoie le message, elle ne sait pas qui va le traiter.

---

# 2. La Structure des Données

- **Topic** : Une catégorie (ex: `transactions-entrees`).
- **Partition** : Un topic est divisé en plusieurs fichiers sur plusieurs serveurs. C'est ce qui permet le parallélisme.
- **Offset** : Un numéro séquentiel unique à chaque message dans une partition.

---

# 3. Consumer Groups

Plusieurs instances de notre moteur peuvent lire le même topic ensemble. Kafka répartit les partitions entre elles.

> [!TIP]
> Si vous avez 3 partitions et 3 serveurs, chaque serveur traite 1/3 du trafic. C'est la puissance du **Scalabilité Horizontale**.

---

# 🏗️ Application : Setup de l'Infrastructure

Nous allons utiliser Docker Compose pour lancer un broker Kafka local. Ce sera le point d'entrée de notre futur moteur de clearing temps réel.

---

# 🧠 Quiz Rapide

1. Kafka est-il une base de données ? (Non, c'est un journal de messages distribué).
2. Que se passe-t-il si un message est lu par un Consumer ? (Il reste dans Kafka pendant une durée définie, il n'est pas supprimé après lecture).
3. À quoi sert l'Offset ? (À savoir quel message a déjà été traité pour ne pas le traiter deux fois).

---

# 📝 Résumé du Jour

- Kafka permet de passer du "Batch" au "Streaming".
- L'architecture est basée sur des journaux persistants.
- Les partitions sont la clé de la performance.
- Demain, nous allons "pousser" des transactions dans Kafka.

**Prochaine étape** : Lancer ton premier Kafka dans le TP 71 !
