# Semaine 15 — Stream Processing avec Kafka

## Objectif

Faire évoluer le Clearing Engine v2.3 vers un v3.0 événementiel avec Apache
Kafka en mode KRaft. Conserver le cœur Scala 3 pur, publier output ou DLQ avant
le commit, et démontrer honnêtement une sémantique at-least-once.

## Notions à maîtriser

- Broker KRaft, topic, partition, offset, clé et consumer group.
- Différence entre ordre par partition et absence d'ordre global.
- Producer avec `acks=all`, idempotence, callback et fermeture sûre.
- Consumer avec auto-commit désactivé et `commitSync` ciblé.
- Convention Kafka : commit de `dernier offset traité + 1`.
- Publication confirmée avant commit et fenêtres de crash.
- Déduplication en mémoire versus état idempotent durable.
- DLQ, fingerprint et protection des données sensibles.
- Healthcheck Docker, listeners hôte/conteneur et preuve reproductible.

## Exercices

- [x] J1 — Lancer Kafka 4.3.0 en KRaft, créer trois topics de trois
  partitions et prouver un round-trip console avec consumer group.
- [x] J2 — Définir le contrat JSON, réutiliser le railway v2.3 et produire 50
  événements à 10/s avec clé, header, idempotence et callbacks.
- [x] J3 — Consommer par groupe, router output/DLQ et committer manuellement
  l'offset suivant par partition.
- [x] J4 — Implémenter le cache de déduplication, tester les fenêtres de crash,
  faire échouer une mutation et démontrer un replay Kafka.
- [x] J5 — Qualifier 1 000 événements déterministes, documenter les limites et
  comparer Kafka au fichier et à HTTP.

## Livrable v3.0

Le paquet `clearing.v30` adapte `RecordEnvelope` à
`TypedRailwayEngine.processLine`; il ne redéfinit aucun type métier. Le JSON
d'entrée garde les huit champs du fil rouge. La sortie ne contient que les
hashes IBAN. La DLQ remplace le payload original par son fingerprint SHA-256.

`BatchCoordinator` traite chaque partition dans l'ordre. Un échec de
publication bloque seulement la suite de cette partition. Un succès autorise
le marquage puis rend `offset + 1` committable. `ConsumerBatchRunner` appelle le
committer après ces étapes. Après un échec, il fait aussi `seek` vers le premier
offset non traité avant le poll suivant.

Docker Compose sépare `localhost:9092` et `kafka:29092`. Le service init crée
`clearing-input`, `clearing-output` et `clearing-dlq`; le healthcheck interroge
le broker API.

## Critères de validation

- [x] KRaft fonctionne sans ZooKeeper.
- [x] Les trois topics possèdent trois partitions et réplication 1 documentée
  comme limite mono-broker.
- [x] Le listener hôte et le listener conteneur sont distincts.
- [x] Le healthcheck contacte le broker réel.
- [x] Le producer utilise `acks=all` et `enable.idempotence=true`.
- [x] La clé Kafka est la banque sender et le header contient l'ID.
- [x] Les callbacks de tous les envois sont terminés avant fermeture.
- [x] Le scénario J2 produit et acquitte 50 événements à 10/s.
- [x] Le consumer désactive l'auto-commit.
- [x] Output ou DLQ est acquitté avant le marquage et le commit.
- [x] Les offsets committés valent `dernier traité + 1` par partition.
- [x] Un échec de publication ne marque pas l'ID et ne dépasse pas le record.
- [x] Un `MockConsumer` sur deux polls prouve le `seek` avant reprise.
- [x] Un doublon marqué n'est pas republié dans le même processus.
- [x] Le même ID avec un autre fingerprint produit un conflit DLQ.
- [x] Le redémarrage perd volontairement le cache et peut republier.
- [x] La mutation `mark avant publish` fait échouer deux tests.
- [x] Un replay Kafka réel produit un doublon observable.
- [x] Le gate fixe produit 1 000 événements acquittés.
- [x] Le consumer classe 900 succès et 100 rejets, sans partition en échec.
- [x] Le groupe termine avec lag 0 sur les trois partitions.
- [x] Le netting des 900 succès a une somme globale nulle.
- [x] Key, headers et value de output/DLQ contiennent zéro IBAN brut.
- [x] La suite S1–S15 passe sous Java 21.
- [x] La suite S1–S15 passe sous Java 17 Docker.
- [x] La revue mentor ne conserve aucun point critique ou important.

## Journal TDD

- Le contrat J1 a d'abord échoué parce que Compose et le script de topics
  n'existaient pas; quatre tests passent après le démarrage réel.
- Le codec et le processor ont d'abord échoué sur tous les types v3.0 absents;
  sept tests couvrent ensuite round-trip, railway, DLQ et panne de hash.
- Le producer a d'abord échoué sur le client Kafka, la configuration, le
  générateur et le pacing absents; six tests ont précédé la preuve 50@10/s.
- Le coordinateur a d'abord échoué sur les ports, offsets et registre absents.
  Les tests couvrent maintenant deux partitions, l'arrêt local et le doublon.
- Les tests de crash sont passés au vert, puis une mutation volontaire a remis
  le marquage avant la publication : deux tests sont redevenus rouges.
- Le gate J5 a d'abord échoué sur le générateur mixte et le CLI unifié absents;
  il classe maintenant 1 000 événements avec seed fixe.

## Preuves

- `fil-rouge/preuves/s15-j1-kraft.md`
- `fil-rouge/preuves/s15-j3-consumer.md`
- `fil-rouge/preuves/s15-j4-at-least-once.md`
- `fil-rouge/preuves/s15-j5-v30.md`

## Validation mentor

**Décision : semaine validée le 14 juillet 2026.**

La suite complète passe sous Java 21 et Java 17 Docker : 515 tests, 84 suites,
0 échec dans chaque environnement. Le gate Kafka propre confirme 1 000 entrées,
900 sorties, 100 DLQ, lag nul et zéro IBAN brut dans clé, headers ou valeur.

La première revue senior a trouvé un point critique et deux points importants :
absence de `seek` après une publication échouée, fuite possible par la clé
Kafka héritée de l'entrée et collision de déduplication entre deux payloads de
même ID. La contre-revue confirme leur fermeture : 0 point critique et 0 point
important. Les 38 tests v3.0 passent dans 9 suites.

Point mineur reporté à la semaine d'observabilité : le mode continu ne journalise
pas encore les compteurs de retry et de doublons de chaque batch. Cette limite
ne modifie pas la sémantique at-least-once validée ici.
