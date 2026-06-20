# Clearing Engine — starter cumulatif S14-S20

Ce projet stabilise le fil rouge construit pendant les semaines 1 à 13. Il sert de base commune aux starter kits S14-S20.

## Contrat repris des semaines précédentes

- S5 introduit `Transaction` et les erreurs métier.
- S10 fixe le pipeline `Either[ClearingError, Transaction]`.
- S11 remplace les primitives par `TransactionId`, `BankCode` et `Money`.
- S12 certifie le netting pur par des propriétés.
- S13 introduit la concurrence, sans modifier le domaine.

La migration de l’identifiant est désormais explicite : l’ancien `Int` devient un `TransactionId` opaque fondé sur `String`. Les frontières JSON, Kafka, Cassandra et HTTP utilisent des DTO versionnés et des primitives. Le cœur métier conserve les types opaques.

## Progression

| Semaine | Version | Ajout |
|---|---:|---|
| 14 | v2.4 | ZIO, services, erreurs typées et ressources |
| 15 | v3.0 | contrats Kafka v1, sortie, DLQ et commits manuels |
| 16 | v3.1 | reprise durable et projections réparables |
| 17 | v3.2 | corrélation, métriques et traces |
| 18 | v3.3 | charge, pannes et ingestion HTTP |
| 19 | v4.0.0-rc1 | API Tapir, image et CI |
| 20 | v4.0.0 | validation finale et handover |

## Commandes

```bash
sbt test
sbt run
sbt assembly
```

Les méthodes marquées `TODO` appartiennent au stagiaire. Le projet doit toujours compiler avant et après chaque TP.
