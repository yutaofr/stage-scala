# TP Jour 3 : Le Grand Assemblage

**Durée :** ~4h | **Fil Rouge :** Intégration Kafka + ZIO + Cassandra

---

## Exercice 1 : Le Consumer-Persister (2h)

1. Crée un programme `ClearingPipelineApp`.
2. Il doit écouter le topic `clearing-transactions`.
3. Pour chaque transaction valide reçue :
   - Calcule la position nette.
   - Enregistre la transaction brute dans Cassandra.
   - Affiche un log de confirmation.

---

## Exercice 2 : Gestion de Panne (1h)

1. Coupe ton container Cassandra tout en laissant Kafka et ton moteur tourner.
2. Observe comment ton moteur ZIO gère l'échec d'écriture (il doit rester bloqué sans commiter les messages Kafka).
3. Relance Cassandra et vérifie que le moteur reprend le traitement là où il s'était arrêté.

---

## Exercice 3 : Monitoring Réel (1h)

1. Envoie 500 transactions via ton simulateur.
2. Vérifie dans `cqlsh` que les 500 lignes sont bien présentes dans ta table.
3. Vérifie que ton moteur n'a pas crashé malgré la charge.

**Livrable** : Code source de l'application connectée de bout en bout et rapport sur le test de crash/reprise.
