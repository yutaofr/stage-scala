# Semaine 16 — Persistance Cassandra et reprise durable

## Objectif

Faire évoluer le Clearing Engine v3.0 vers un v3.1 capable de reprendre un
événement après redémarrage, de réparer des projections idempotentes et de
répondre aux requêtes clearing sans scan applicatif caché. Conserver les
garanties Kafka de S15 et ne prétendre ni exactly-once, ni haute disponibilité.

## Notions à maîtriser

- Keyspace, datacenter, réplication et limite d'un nœud unique.
- Modélisation Cassandra orientée requêtes et dénormalisation.
- Clé de partition, clustering, buckets et absence d'ordre global.
- Prepared statements, `CompletionStage`, `executeAsync` et fan-out borné.
- Upsert idempotent, fingerprint et contribution stable par événement.
- Machine d'état `Received → Projected → Completed`.
- Fenêtres de crash entre Cassandra, publication Kafka et commit d'offset.
- Reprise avec le même consumer group et replay depuis un nouveau groupe.
- Confidentialité des tables, output et DLQ.

## Exercices

- [x] J1 — Lancer Cassandra 4.1.11, créer un keyspace avec
  `NetworkTopologyStrategy` et modéliser cinq tables par requête.
- [x] J2 — Définir un repository asynchrone, une session partagée, des
  statements préparés et des projections idempotentes.
- [x] J3 — Orchestrer `Received`, projections, `Projected`, ack, `Completed`,
  commit et reprise de partition; faire échouer une mutation de l'ordre.
- [x] J4 — Lire historique bucketé, mouvements et position banque/jour, puis
  agréger le top des paires sans `ALLOW FILTERING`.
- [x] J5 — Qualifier 500 records, couper Cassandra pendant le traitement,
  reprendre avec le même groupe et prouver un replay durable complet.

## Livrable v3.1

Le paquet `clearing.v31` réutilise le domaine v2.3 et les frontières Kafka
v3.0. `DurableRecordProcessor` calcule une décision, charge tous les états du
même event key, crée `Received`, écrit les projections, passe à `Projected`,
attend l'ack output ou DLQ, puis crée `Completed`. Seul ce dernier état rend
`offset + 1` committable.

`LiveCassandraRepository` prépare les requêtes une fois et utilise uniquement
les APIs asynchrones du driver Apache. Les cinq tables répondent à des usages
explicites. Une contribution de position porte la clé stable de l'événement;
un replay réécrit donc la ligne au lieu de modifier un compteur.

Le Compose v3.1 lance Kafka KRaft, Cassandra, les trois topics et le schéma CQL.
Le CLI unifié fournit `producer`, `qualify`, `consumer` et `report`.

## Critères de validation

- [x] Cassandra 4.1.11 et le driver Apache 4.19.3 sont fixés explicitement.
- [x] Le keyspace utilise `NetworkTopologyStrategy` et documente RF=1.
- [x] Les cinq tables ont des clés adaptées aux requêtes demandées.
- [x] Le schéma n'utilise ni `ALLOW FILTERING`, ni index secondaire, ni
  compteur Cassandra.
- [x] Les IBAN bruts ne sont stockés dans aucune table.
- [x] La session et les statements sont partagés pendant le processus.
- [x] Le repository expose des `CompletionStage` et appelle `executeAsync`.
- [x] La boucle borne les effets actifs avec `max.poll.records=1`.
- [x] Les projections valides créent 1 historique, 2 mouvements, 2
  contributions et 1 activité de paire.
- [x] Un replay exact ne double aucune projection.
- [x] Le même ID avec un autre fingerprint devient `EVENT_ID_CONFLICT` durable.
- [x] Un JSON sans ID possède une clé durable dérivée de son fingerprint.
- [x] `Completed` arrive après l'ack output/DLQ et avant le commit.
- [x] La mutation `Completed avant publish` fait échouer trois tests.
- [x] Les fenêtres `Received`, projection, `Projected` et ack sont testées.
- [x] Une panne bloque localement la partition et provoque un `seek`.
- [x] Une coupure Cassandra réelle interrompt un traitement partiel sans sauter
  les 491 records restants.
- [x] Le même groupe reprend ensuite jusqu'au lag 0 sur trois partitions.
- [x] Le gate réconcilie 500 inputs, 485 outputs, 5 DLQ et 10 replays.
- [x] Les 485 succès donnent 970 contributions et une somme globale de 0.00.
- [x] Un nouveau groupe relit 500 doublons sans publication ni projection.
- [x] Output, DLQ et historique contiennent zéro IBAN brut.
- [x] Le test live force `pageSize=1` et traverse plusieurs pages.
- [x] La suite S1–S16 passe sous Java 21 : 546 tests, 94 suites, 0 échec.
- [x] La suite S1–S16 passe sous Java 17 Docker : 546 tests, 94 suites,
  0 échec.
- [ ] La revue mentor ne conserve aucun point critique ou important.

## Journal TDD

- Le contrat Compose/CQL a d'abord échoué sur quatre fichiers ou règles
  absents, puis a fixé versions, healthchecks, dépendances et schéma.
- Le repository a d'abord échoué à la compilation sur les types v3.1 absents.
  Le fake puis Cassandra exécutent les mêmes clés et invariants.
- Les tests du repository live écrivent deux fois le même événement et forcent
  `pageSize=1` pour traverser les pages asynchrones.
- La machine d'état a d'abord échoué à la compilation. Une fois verte, la
  mutation `Completed avant publish` a fait échouer trois scénarios.
- Les tests Kafka/report ont précédé la boucle v3.1, le CLI et la résolution
  des partitions reprises.
- Le scénario de qualification a d'abord échoué sur le générateur absent; il
  fixe maintenant 485 valides, 5 invalides et 10 replays.

## Preuves

- `fil-rouge/preuves/s16-j1-schema.md`
- `fil-rouge/preuves/s16-j2-repository.md`
- `fil-rouge/preuves/s16-j3-reprise.md`
- `fil-rouge/preuves/s16-j4-requetes.md`
- `fil-rouge/preuves/s16-j5-v31.md`
- `fil-rouge/retro_s16.md`

## Validation mentor

**Décision : qualification technique terminée, revue senior en cours.**

Les gates Java 21, Java 17, Cassandra live, coupure/reprise, replay et
confidentialité sont verts. La semaine ne sera marquée validée qu'après la
contre-vérification senior et la fermeture de tout point critique ou important.
