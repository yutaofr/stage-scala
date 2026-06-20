# TP Jour 5 : L'Union fait la Force

**Durée :** ~4h | **Fil Rouge :** Cluster de clearing horizontal

---

## Point de départ

- Copie le **Kit 18.5** de `starter_kit_s18.md`.
- Vérifie que le topic possède au moins trois partitions.
- Distingue continuité du traitement applicatif et disponibilité de Kafka/Cassandra.

## Exercice 1 : Scalabilité Docker (1h)

1. Utilise `docker compose up -d --scale engine=3` pour lancer trois exemplaires du moteur.
2. Vérifie l'affectation des partitions dans les logs.
3. Ne suppose pas un tiers exact du volume si les partitions ne portent pas le même trafic.

**Validation :** chaque partition appartient à une seule instance du groupe.

---

## Exercice 2 : Test de Résilience (1h30)

1. Lance ton simulateur à fond.
2. Coupe brutalement une des 3 instances (`docker stop`).
3. Mesure la durée du rebalance et l'évolution du lag.
4. Vérifie les IDs uniques dans Cassandra et les éventuelles relectures.

**Validation :** le rapport sépare transactions manquantes, dupliquées et relues.

---

## Exercice 3 : Zero Downtime (1h30)

1. Construis une nouvelle image avec un tag distinct.
2. Remplace les instances une par une.
3. Mesure le débit et le lag pendant l'opération.
4. Vérifie qu'au moins une instance consomme pendant toute la fenêtre.
5. Note que Docker Compose n'est pas un orchestrateur de rolling update automatique.

**Validation :** la continuité est prouvée par les métriques.

**Livrable** : Rapport de déploiement en cluster et preuve que le système survit à l'arrêt d'un nœud sans perte de données.
