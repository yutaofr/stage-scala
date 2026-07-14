# Marc Semaine 15 — Conception du Clearing Engine v3.0 Kafka

## Portée du stage

La semaine 15 est le premier jalon distribué du fil rouge de Marc. Les semaines
13 et 14 restent explicitement ignorées parce qu'elles sont centrées sur Pekko
et ZIO. Le v3.0 part donc du cœur actif v2.3 et n'invente aucun v2.4 implicite.
Il n'ajoute ni Cats, ni Pekko, ni ZIO.

L'objectif pédagogique est de distinguer trois responsabilités : le cœur de
clearing pur, les adaptateurs Kafka et le processus d'exécution. Le domaine
opaque, la validation, l'anonymisation, le netting et les serializers des
paquets `clearing.v22` et `clearing.v23` restent la source de vérité. Le paquet
`clearing.v30` ne redéfinit ni banque, ni transaction, ni argent.

## Architecture v3.0

Le flux nominal est :

```text
clearing-input
  -> décodage JSON
  -> TypedRailwayEngine v2.3
  -> décision validée ou rejetée
  -> clearing-output ou clearing-dlq
  -> accusé Kafka
  -> marquage de déduplication
  -> commit de l'offset suivant, par partition
```

`RecordEnvelope` conserve le topic, la partition, l'offset, la clé, les
headers, la valeur et `occurredAt: Instant`. Cette date stable prépare le
contrat Cassandra de la semaine 16 sans introduire Cassandra en S15.

Le JSON d'entrée reprend les huit champs historiques : `id`, `sender`,
`receiver`, `sourceIban`, `destinationIban`, `amount`, `transactionType` et
`currency`. La clé Kafka est le code de la banque émettrice afin de conserver
l'ordre de ses opérations dans une partition. Le header `transaction-id`
permet le diagnostic sans décoder le payload.

Le processeur transforme le JSON en la ligne CSV canonique attendue par
`TypedRailwayEngine.processLine`. Cette adaptation reste à la frontière : les
règles métier et l'anonymisation ne sont pas recopiées. Une transaction valide
produit un événement ne contenant que les hashes IBAN. Un rejet produit un
événement DLQ avec ID éventuel, code, message stable et empreinte SHA-256 du
payload. Le payload original et les IBAN bruts ne sont jamais publiés dans la
DLQ ni écrits dans les logs.

## Livraison et offsets

Le producteur configure `acks=all` et `enable.idempotence=true`. Il utilise le
callback Kafka pour compter les succès et erreurs et attend toutes les réponses
avant la fermeture. Le simulateur produit d'abord 50 transactions déterministes
à 10 événements par seconde, puis accepte un volume et une seed explicites pour
le test de 1 000 événements.

Le consumer désactive l'auto-commit. Dans chaque partition, il traite les
records dans l'ordre et s'arrête au premier échec technique. Il publie la
décision vers la sortie ou la DLQ, attend l'accusé, marque ensuite l'ID dans le
registre de déduplication, puis autorise le commit de `dernier offset traité +
1`. Les offsets de partitions différentes sont calculés séparément. Un record
rejeté fonctionnellement est un record traité : il est envoyé en DLQ puis son
offset peut être committé.

Un `BatchCoordinator` testable contient cette règle indépendamment des classes
Kafka. Les adaptateurs `KafkaDecisionPublisher`, `KafkaOffsetCommitter` et
`KafkaConsumerLoop` portent les effets. Cette séparation permet de prouver les
cas de crash sans mocker les classes finales du client Kafka.

## Sémantique at-least-once

Le registre en mémoire est volontairement pédagogique. Un ID n'est enregistré
qu'après l'accusé de l'événement de sortie. Il évite une répétition observée
tant que le processus reste vivant, mais il est perdu au redémarrage. Un crash
après la publication et avant le commit peut donc republier le même résultat.
Le v3.0 revendique uniquement une livraison at-least-once, jamais un
exactly-once externe.

Le contrat préparatoire S16 sera `Received -> Projected -> Completed` dans un
stockage durable. Il remplacera le registre mémoire sans changer
`RecordEnvelope`, `ProcessingDecision` ni l'ordre publication-confirmation-
commit.

## Kafka local et reproductibilité

Docker Compose lance un broker Apache Kafka 4.3.0 en mode KRaft, sans
ZooKeeper. Trois partitions et un facteur de réplication 1 sont acceptés
uniquement pour ce laboratoire à un seul broker. Les topics sont
`clearing-input`, `clearing-output` et `clearing-dlq`.

Deux listeners séparent les usages : `localhost:9092` pour SBT lancé sur
l'hôte et `kafka:29092` pour les conteneurs. Un service `kafka-init` attend le
broker, crée les topics de façon idempotente et vérifie leur description. Le
healthcheck ne se contente pas d'un port ouvert : il interroge réellement le
broker.

## Tests et acceptation

Les tests unitaires couvrent le codec, la réutilisation du railway, la sûreté
de la DLQ, les propriétés producteur, la déduplication et toutes les fenêtres
de crash. Les tests d'intégration Docker prouvent la santé KRaft, les trois
topics, les trois partitions, la production/consommation, les groupes et les
offsets.

Le gate final produit 1 000 transactions avec seed fixe et vérifie :

- `produced = valid + rejected` ;
- chaque record consommé aboutit à output ou DLQ avant commit ;
- le netting de tous les événements valides a une somme globale nulle ;
- une reprise après crash rejoue un record non committé ;
- aucun IBAN brut n'apparaît dans output, DLQ ou logs ;
- les suites S1–S12 restent vertes sous Java 21 et Java 17.

La rétrospective compare Kafka au traitement fichier et à HTTP : Kafka apporte
un journal durable, le partitionnement, les groupes et le replay, mais le
laboratoire reste mono-broker, sans réplication réelle, sans sécurité réseau et
avec une déduplication non durable.
