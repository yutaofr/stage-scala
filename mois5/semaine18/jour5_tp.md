# TP Jour 5 : L'Union fait la Force

**Durée :** ~4h | **Fil Rouge :** Cluster de clearing horizontal

---

## Exercice 1 : Scalabilité Docker (1h)

1. Utilise la commande `docker-compose up --scale engine=3` pour lancer 3 exemplaires de ton moteur.
2. Vérifie dans les logs que chaque moteur reçoit un tiers des messages (grâce aux IDs de partitions).

---

## Exercice 2 : Test de Résilience (1h30)

1. Lance ton simulateur à fond.
2. Coupe brutalement une des 3 instances (`docker stop`).
3. Observe les logs des 2 rescapées : elles doivent râler brièvement (Rebalance) puis reprendre tout le trafic.
4. Pas une seule transaction ne doit être manquante dans Cassandra.

---

## Exercice 3 : Zero Downtime (1h30)

1. Modifie ton code (ex: change un log).
2. Refais une image Docker.
3. Utilise une stratégie de "Rolling Update" (arrêter un moteur, le mettre à jour, le relancer, puis passer au suivant).
4. Vérifie que pendant toute l'opération, le clearing n'a jamais été arrêté.

**Livrable** : Rapport de déploiement en cluster et preuve que le système survit à l'arrêt d'un nœud sans perte de données.
