# Preuve S16 J1 — Schéma Cassandra orienté requêtes

Date : 14 juillet 2026

## Choix vérifiés

- Cassandra est fixé à `4.1.11` et le Java driver Apache à `4.19.3`.
- Le keyspace `clearing` utilise `NetworkTopologyStrategy` avec
  `datacenter1: 1`.
- Le laboratoire garde un seul nœud : la réplication à 1 ne tolère aucune panne
  de nœud.
- Chaque lecture possède sa table et sa clé de partition. Le schéma n'utilise
  ni `ALLOW FILTERING`, ni index secondaire, ni compteur incrémental.
- `processing_state` regroupe les fingerprints d'un même `event_key`. Cette
  partition distingue replay exact et réutilisation conflictuelle d'un ID.
- Les contributions de position portent `event_key`; un replay réécrit la même
  ligne au lieu d'ajouter une seconde contribution.

## Requêtes couvertes

| Besoin | Table |
|---|---|
| reprise durable | `processing_state` |
| historique date + bucket | `clearing_history_by_day` |
| mouvements banque + date | `transactions_by_bank_day` |
| position banque + date | `bank_positions` |
| activité des paires par date | `pair_activity_by_day` |

La date provient de `RecordEnvelope.occurredAt` en UTC. La clé reste donc
stable lors d'un replay Kafka.

## Commandes

```bash
sbt 'testOnly clearing.v31.CassandraComposeContractSpec'
docker compose -f docker/docker-compose-v31.yml config --quiet
```

Le test de contrat contrôle les versions, healthchecks, dépendances, tables et
interdictions du schéma. La preuve runtime Cassandra sera ajoutée après
l'implémentation du repository.

Les variables `CASSANDRA_KEYSPACE` et `CASSANDRA_DATACENTER` configurent la
connexion du processus. Le script Compose initialise volontairement
`clearing/datacenter1`. Une valeur différente exige donc de créer au préalable
un schéma compatible dans le datacenter ciblé ; le script local n'est pas un
moteur de migration paramétrable.
