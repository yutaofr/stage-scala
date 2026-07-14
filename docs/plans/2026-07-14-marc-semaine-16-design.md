# Marc Semaine 16 — Design Cassandra v3.1

## Contexte et périmètre

Marc part du Clearing Engine v3.0 validé en semaine 15. Cette version possède
déjà un producer Kafka fiable, un consumer à commit manuel, un traitement
ordonné par partition, un `seek` après échec et une sortie sans IBAN brut. Son
registre de déduplication reste cependant en mémoire : un redémarrage perd
l'état et peut republier un événement.

La semaine 16 remplace ce registre par un état Cassandra et ajoute des
projections adaptées aux requêtes du fil rouge. Elle n'introduit ni Cats, ni
Pekko, ni ZIO. Elle ne traite pas encore Prometheus, Grafana, la performance
multi-instance ou une API HTTP : ces sujets appartiennent aux semaines
suivantes.

Trois options ont été étudiées :

1. Persister seulement la clé de déduplication. Cette option est simple, mais
   elle ne répare pas une projection partiellement écrite et ne satisfait pas
   les requêtes du tableau de bord.
2. Persister un état unique avec les données métier dans une seule table. Cette
   option facilite la reprise, mais impose des scans ou `ALLOW FILTERING` pour
   les lectures par banque et par date.
3. Conserver un état de traitement et écrire une table par requête. Cette
   option dénormalise les données, mais rend la reprise et les lectures
   explicites. C'est l'option retenue.

Le laboratoire utilise Cassandra 4.1.11 avec un seul nœud et une réplication à
1. Il adopte néanmoins `NetworkTopologyStrategy`, avec `datacenter1: 1`, afin
que le schéma exprime correctement le datacenter. Cette configuration ne
prouve aucune tolérance à la panne.

## Architecture et flux

Le paquet `clearing.v31` adapte les composants v3.0 au stockage durable. Il
réutilise `RecordEnvelope`, `V30RecordProcessor`, `ProcessingDecision`, les
ports Kafka et les règles métier v2.3. Il ne copie pas le starter kit S16 et ne
redéfinit aucun type métier.

Le chemin d'un record est le suivant :

```text
poll Kafka
  → calculer decision + eventKey + payload fingerprint
  → lire processing_state
  → marquer Received si ce fingerprint est nouveau
  → écrire ou réparer les projections idempotentes
  → marquer Projected
  → publier output ou DLQ et attendre l'ack Kafka
  → marquer Completed
  → autoriser offset + 1
  → commitSync par partition
```

Un record déjà `Completed` avec le même fingerprint est un doublon durable :
le consumer ne réécrit pas les projections et ne republie pas la décision,
mais il autorise l'avancement de l'offset. Si le même ID arrive avec un autre
fingerprint, le record suit son propre état durable et produit un
`EVENT_ID_CONFLICT` en DLQ. Un JSON sans ID reçoit une clé
`invalid:<fingerprint>` afin que sa DLQ puisse aussi devenir durablement
idempotente.

Le driver Apache Cassandra expose `CompletionStage`. Le repository prépare les
statements une fois par session et conserve des méthodes asynchrones. Les
écritures indépendantes d'une projection sont lancées ensemble, puis réunies
avec `CompletableFuture.allOf`. La boucle Kafka attend la fin d'un seul record à
la fois avec `max.poll.records=1`. Cette borne donne une backpressure simple et
empêche l'application de créer un nombre non borné de futures.

## Modèle Cassandra orienté requêtes

Le keyspace `clearing` contient cinq tables :

| Requête ou invariant | Table | Clé de partition | Clustering |
|---|---|---|---|
| reprendre un événement | `processing_state` | `event_key` | `payload_fingerprint` |
| historique d'un jour | `clearing_history_by_day` | `clearing_date, bucket` | `occurred_at, event_key` |
| mouvements d'une banque | `transactions_by_bank_day` | `bank_id, clearing_date` | `occurred_at, event_key, direction` |
| position d'une banque | `bank_positions` | `bank_id, clearing_date` | `event_key` |
| activité d'une paire | `pair_activity_by_day` | `clearing_date` | `bank_pair, event_key` |

L'historique répartit une journée sur 16 buckets calculés par
`floorMod(eventKey.hashCode, 16)`. Ce nombre fait partie de la version du
schéma. La lecture journalière interroge explicitement les 16 partitions et
fusionne les résultats dans l'application.

Une transaction validée produit deux mouvements et deux contributions de
position : un débit négatif pour le sender et un crédit positif pour le
receiver. `bank_positions` ne contient pas un compteur Cassandra incrémental.
Chaque ligne porte la contribution déterministe d'un événement. Le repository
additionne les contributions à la lecture. Rejouer la même clé remplace donc la
même ligne au lieu de compter deux fois.

La paire de banques est triée lexicalement avant écriture. Le top 10 du
laboratoire lit la partition du jour, agrège par paire et trie en Scala. Cette
méthode est correcte pour le volume pédagogique ; un classement pré-calculé et
bucketé serait nécessaire pour une forte cardinalité.

Le schéma ne stocke jamais les IBAN bruts. L'historique contient seulement les
identifiants, banques, montants de règlement, statut et timestamps nécessaires
aux requêtes définies.

## Pannes et garanties

Les transitions ne prétendent pas former une transaction entre Cassandra et
Kafka. Elles rendent chaque fenêtre de panne explicite :

- échec avant `Received` : aucun offset n'avance ; le record repart de zéro ;
- échec pendant les projections : l'état reste `Received` ; le replay réécrit
  les mêmes clés et répare les lignes manquantes ;
- échec après `Projected` : le replay saute les projections et retente la
  publication ;
- échec après l'ack Kafka mais avant `Completed` : la sortie peut être publiée
  deux fois ; aucune sortie n'est perdue ;
- échec après `Completed` mais avant le commit : le replay reconnaît le
  doublon durable et autorise le commit sans republier ;
- indisponibilité Cassandra : la partition est remise sur son premier offset
  non terminé avec `seek`; les autres partitions peuvent progresser.

Le système garantit donc at-least-once entre l'entrée et la sortie, avec des
projections Cassandra idempotentes. Il ne garantit pas exactly-once entre deux
systèmes externes. Les erreurs de repository remontent jusqu'au coordinateur ;
elles ne deviennent jamais un succès métier ni un commit.

La session Cassandra est créée une fois et fermée après le consumer et le
producer. Les paramètres `CASSANDRA_HOST`, `CASSANDRA_PORT`,
`CASSANDRA_DATACENTER` et `CASSANDRA_KEYSPACE` restent configurables. Le mode
local utilise `localhost:9042`; un processus dans Compose utiliserait
`cassandra:9042`.

## Validation et livrables

Chaque journée conserve une preuve dans `stagiaires/marc/fil-rouge/preuves/` :

- J1 : schéma, tables, clés et requêtes CQL réelles ;
- J2 : repository asynchrone, session partagée et fan-out borné ;
- J3 : ordre des étapes, panne Cassandra, commit et `seek` ;
- J4 : lectures banque/jour, historique bucketé et top des paires ;
- J5 : 500 entrées déterministes, doublons connus, invalides, restart et
  reprise sans double projection.

Les tests purs utilisent un repository mémoire qui respecte les mêmes clés et
transitions. Les tests d'intégration utilisent le vrai driver contre Cassandra
Docker. Le gate final recrée les volumes, exécute le schéma, lance Kafka et
Cassandra, traite le lot, redémarre le consumer avec le même groupe après une
fenêtre de panne contrôlée, puis vérifie :

- aucun `Completed` sans projections et ack de publication ;
- aucune contribution Cassandra dupliquée après replay ;
- aucune progression d'offset avant `Completed` ;
- 500 entrées classées avec comptes réconciliés et lag final nul ;
- positions globales équilibrées ;
- zéro IBAN brut dans Cassandra, output et DLQ ;
- suites S1–S16 vertes sous Java 21 et Java 17 Docker ;
- revue senior sans point critique ou important.

Le livrable est le Clearing Engine v3.1, son Compose Kafka+Cassandra, son schéma
CQL, ses preuves de reprise, sa rétrospective et le suivi hebdomadaire. Après
validation, le jalon local sera `marc-v3.1`. Aucun commit ni tag ne sera poussé
sans demande explicite.
