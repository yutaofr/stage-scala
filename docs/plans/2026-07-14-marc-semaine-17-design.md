# Marc Semaine 17 — Design observabilité v3.2

## Contexte et périmètre

Marc part du Clearing Engine v3.1 validé en semaine 16. Le consumer lit Kafka,
persiste un état de reprise et des projections Cassandra, publie la décision,
puis autorise le commit. La semaine 17 rend ce chemin observable sans modifier
ses garanties : logs JSON, métriques Prometheus, traces OpenTelemetry,
dashboard Grafana et alertes Alertmanager.

Le périmètre exclut Cats, Pekko et ZIO. Il exclut aussi les optimisations de
charge et l'API métier, prévues en semaines 18 et 19. Un serveur HTTP JDK expose
seulement `/metrics` et `/health`; il ne devient pas une API fonctionnelle.

Trois stratégies ont été étudiées :

1. Ajouter les appels Logback, Micrometer et OpenTelemetry directement dans
   v3.1. Cette solution est courte, mais couple la logique durable aux outils
   et rend les tests métier dépendants de leur configuration.
2. Entourer le processor v3.1 d'un seul wrapper. Cette solution protège le code
   existant, mais ne peut pas distinguer les étapes réelles `parse`, `validate`,
   `netting` et `persist`.
3. Ajouter des ports d'observation no-op aux frontières existantes, puis les
   lier aux outils dans `clearing.v32`. Cette solution conserve le comportement
   des anciennes versions et produit des signaux à l'endroit où le travail a
   réellement lieu. C'est l'option retenue.

## Architecture des signaux

Le paquet `clearing.v32` assemble les composants v3.1 et leurs observateurs. Les
ports restent synchrones et génériques : les paquets `v22`, `v30` et `v31` ne
dépendent ni de Micrometer, ni d'OpenTelemetry.

```text
poll Kafka
  → extraire traceparent et ouvrir clearing.consume
  → MDC topic/partition/offset/txId
  → span parse : décoder l'événement et la ligne typée
  → span validate : appliquer les règles métier
  → span netting : enrichir, convertir, calculer les frais et anonymiser
  → span persist : lire/réparer l'état durable et ses projections
  → publier output/DLQ avec traceparent injecté
  → marquer Completed
  → compteur, durée, lag et log de résultat
  → commitSync ou seek selon le résultat v3.1
```

`RailwayStageObserver` expose une opération `around(stage)(body)` avec un
singleton no-op. `TypedRailwayEngine` l'appelle autour des trois groupes de
calcul. `DurableStageObserver` encadre le cycle durable dans v3.1. Le port
`ConsumerPollObserver` reçoit le consumer après chaque poll et calcule le lag à
partir de `endOffsets - position` pour les partitions assignées. Tous les
paramètres ont une valeur no-op par défaut; les appels v2.2 à v3.1 conservent
donc leur comportement.

Le span `clearing.consume` est le parent des spans de traitement. Le consumer
extrait le contexte W3C du `RecordEnvelope`; un message sans `traceparent`
valide commence une nouvelle trace. Le publisher injecte le contexte actif
dans les headers du vrai `ProducerRecord`. Les noms de span restent stables et
`tx.id` devient un attribut. Aucun identifiant métier n'entre dans le nom du
span.

## Logs structurés et corrélation

Logback constitue l'unique backend SLF4J. `logback.xml` produit des lignes
lisibles pour le développement. `logback-json.xml`, sélectionné avec
`-Dlogback.configurationFile=...`, produit une ligne JSON par événement avec
les champs stables `timestamp`, `level`, `message`, `service` et `environment`.

`ObservedDurableProcessor` pose `txId`, `topic`, `partition` et `offset` dans le
MDC. Il sauvegarde la map précédente et la restaure dans un `finally`. Deux
virtual threads peuvent donc traiter des records différents sans fuite de
contexte. Les logs ne contiennent ni payload, ni IBAN, ni montant, ni objet
client. Le test utilise le vrai processor durable v3.1 avec un repository
mémoire; il ne remplace pas le chemin métier par un fake vide.

## Métriques et cardinalité

Une seule `PrometheusMeterRegistry` appartient au runtime v3.2. Elle expose :

| Signal | Type | Labels | Sens |
|---|---|---|---|
| `clearing_transactions_processed_total` | counter | `status` | résultat `success`, `duplicate` ou `failure` |
| `clearing_processing_duration_seconds` | histogram | `status` | durée d'un record avec buckets explicites |
| `clearing_kafka_lag_records` | gauge | `topic`, `partition` | somme `endOffset - position` par partition |
| `jvm_memory_used_bytes` | gauge JVM | `area`, `id` | mémoire réellement observée par Micrometer |

Les labels sont finis ou issus du faible nombre de partitions du laboratoire.
Les transaction IDs, offsets, raisons d'erreur et banques n'entrent dans aucun
label. Le serveur JDK répond en texte Prometheus sur `:8080/metrics`. Le
registre et le serveur se ferment avec le consumer, le producer et Cassandra.

## Stack Docker et provisioning

`docker-compose-v32.yml` étend le laboratoire Kafka+Cassandra avec des images
fixées : OpenTelemetry Collector 0.153.0, Jaeger 2.18.0, Prometheus 3.12.0,
Alertmanager 0.32.1 et Grafana 13.0.2. Le build Scala fixe Logback 1.5.38,
Logstash Logback Encoder 9.0, Micrometer 1.16.5 et OpenTelemetry Java 1.63.0.
Le resolveur sbt devra confirmer ces artefacts avant leur adoption.

Le Collector écoute OTLP gRPC et exporte vers Jaeger. Prometheus scrape le
processus hôte via `host.docker.internal:8080`. Grafana provisionne sa datasource
et son dashboard depuis des fichiers montés en lecture seule. Une recréation du
conteneur ne demande donc aucun clic de configuration.

Le dashboard versionné contient cinq panneaux : débit, taux d'erreur, p99,
lag Kafka et mémoire JVM. Chaque panneau fixe son unité, sa fenêtre et son
comportement sans données. La variable `$status` provient des valeurs du label
borné. Le dashboard n'ajoute pas `$bank`, car aucune métrique de banque n'est
nécessaire à l'objectif S17.

## SLO, règles et routage

Le livrable définit deux SLO glissants sur 30 jours :

- disponibilité de traitement : au moins 99,5 % des records aboutissent à
  `success` ou `duplicate`;
- latence : au moins 99 % des records terminent en moins de 500 ms.

Le document SLO précise la population, la source, la fenêtre, le budget
d'erreur et les cas sans trafic, maintenance ou données invalides. Ces cibles
servent au laboratoire; elles ne prétendent pas être des engagements de
production validés par une charge représentative.

Les règles Prometheus protègent toutes les divisions par zéro et fixent une
durée `for`. `EngineDown` utilise `up == 0`; les alertes de taux d'échec et de
latence utilisent les compteurs et histogrammes v3.2. Chaque règle porte
`severity`, `summary` et `description`. `promtool check rules` valide le fichier.

Alertmanager route les alertes vers un receiver webhook local séparé du
Clearing Engine. Arrêter l'application ne coupe donc pas le destinataire. Le
webhook conserve les notifications firing et resolved dans un volume de
preuve. La procédure de résolution relie Prometheus, Alertmanager et la
notification reçue.

## Pannes, limites et garanties

Une panne de l'exporteur de traces ne change jamais le résultat métier. Les
spans utilisent un batch processor borné et peuvent être perdus si le
Collector reste indisponible. Une panne du serveur de métriques n'autorise
aucun offset; le runtime échoue au démarrage plutôt que de fonctionner sans
son contrat d'exploitation.

Les métriques sont en mémoire. Un redémarrage remet les compteurs à zéro; les
requêtes PromQL utilisent donc `rate` et tolèrent les resets. Le lag représente
la vue du consumer actif après poll. Il ne remplace pas une supervision Kafka
multi-groupe telle que Kafka Exporter.

La corrélation ne modifie pas la garantie v3.1. Le chemin reste : poll, état
durable, output/DLQ, `Completed`, puis commit. Les logs, métriques et traces
décrivent ce chemin; ils ne deviennent jamais une condition de succès métier.

## Validation et livrables

Chaque journée conserve une preuve dans `stagiaires/marc/fil-rouge/preuves/` :

- J1 : configuration locale/JSON, extrait parseable et test de fuite MDC;
- J2 : métriques réconciliées, endpoint, cible Prometheus UP puis DOWN;
- J3 : trace complète, propagation Kafka et message sans contexte;
- J4 : datasource et dashboard provisionnés avec données du simulateur;
- J5 : SLO, règles valides, alerte pending/firing/resolved et webhook.

Le gate final repart de volumes propres, lance la stack, traite un lot
déterministe et vérifie :

- une seule implémentation SLF4J et des logs JSON parseables sans donnée
  sensible;
- compteurs réconciliés, labels bornés, histogramme et lag final nul;
- parenté des spans à travers Kafka et présence des quatre étapes réelles;
- cinq panneaux Grafana alimentés après provisioning automatique;
- règles valides et même alerte visible dans Prometheus, Alertmanager et le
  webhook;
- suites S1–S17 vertes sous Java 21 et Java 17;
- revue senior sans point critique ou important.

Le livrable final est le Clearing Engine v3.2, sa stack d'observabilité, ses
dashboards, ses règles, ses preuves et sa rétrospective. Après validation, le
jalon local sera `marc-v3.2`. Aucun commit ni tag ne sera poussé sans demande
explicite.
