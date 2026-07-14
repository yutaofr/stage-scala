# Rétrospective S16 — Cassandra et reprise durable

## Auto-évaluation

| Concept | Confiance (1-5) | Point d'amélioration |
|---|---:|---|
| Modélisation orientée requêtes | 4 | Tester le bucket sizing avec un volume représentatif |
| Driver Cassandra asynchrone | 4 | Mesurer plusieurs opérations concurrentes et leur backpressure |
| Idempotence des projections | 5 | Ajouter une stratégie de migration de schéma |
| Machine d'état durable | 4 | Formaliser les transitions concurrentes entre instances |
| Reprise Kafka/Cassandra | 4 | Tester rebalances, timeouts et plusieurs consommateurs |
| Exploitation Compose | 4 | Passer à un cluster avec réplication réelle |

## Pourquoi une table par requête

Cassandra n'est pas un modèle relationnel que l'application interroge ensuite
avec des filtres arbitraires. Les besoins ont été définis avant les tables :
reprendre un événement, lire l'historique d'un jour, consulter les mouvements
et la position d'une banque, puis classer les paires actives. Chaque accès est
borné par une clé de partition; aucun `ALLOW FILTERING` ne masque un scan.

Les positions sont des contributions par événement, pas des compteurs. Deux
écritures du même `event_key` remplacent la même ligne. Cette décision coûte
une somme à la lecture, mais rend les projections réparables et idempotentes.

## Pourquoi `Completed` vient après l'ack

L'ordre retenu est `Received`, projections, `Projected`, ack Kafka,
`Completed`, puis commit de l'offset suivant. Marquer `Completed` avant l'ack
pourrait faire perdre une sortie : le replay croirait le record terminé. Après
l'ack, un crash avant `Completed` peut au contraire republier la sortie. Cette
duplication possible est la limite assumée de l'at-least-once entre deux
systèmes externes.

Une mutation volontaire a déplacé `Completed` avant la publication. Trois
tests sont alors redevenus rouges : ordre des effets, état après échec de
publication et replay de la fenêtre ack/`Completed`.

## Ce que la coupure réelle a appris

La panne a été déclenchée après 7 états Cassandra, alors que Kafka avait validé
9 offsets. Le processus a échoué au lieu de continuer à committer. Après
redémarrage, le même groupe a traité les 491 records restants et atteint lag 0.
Un groupe neuf a ensuite relu les 500 records sans nouvelle publication ni
nouvelle projection.

Le test réel prouve le comportement du processus et des conteneurs. Les tests
unitaires complètent cette preuve en injectant précisément les pannes après
`Received`, pendant les projections, après `Projected` et entre l'ack et
`Completed`, fenêtres trop courtes pour être ciblées fiablement à la main.

Le run de dix minutes complète cette preuve fonctionnelle par une observation
opérationnelle. Les 30 000 records atteignent lag 0 pendant la cinquième
fenêtre; les cinq fenêtres idle montrent notamment une baisse RSS après GC.
Cette durée reste trop courte pour conclure à l'absence de fuite ou à une
capacité de production.

## Asynchronisme borné

Le port du repository retourne des `CompletionStage` et le driver utilise
`executeAsync`. Les écritures indépendantes d'une projection partent ensemble
et sont réunies par `CompletableFuture.allOf`. La boucle Kafka garde
`max.poll.records=1`; elle attend donc un seul graphe d'effets à la fois. C'est
une backpressure volontairement simple, adaptée au laboratoire mais pas une
qualification de débit.

## Bilan

Le v3.1 remplace la déduplication mémoire par 490 états durables et conserve
485 projections valides équilibrées après panne et replay complet. Les 5 JSON
invalides restent corrélables par fingerprint, sans payload ni IBAN brut. La
prochaine étape autorisée est l'observabilité Prometheus/Grafana; la haute
disponibilité et la performance multi-instance restent à qualifier plus tard.
