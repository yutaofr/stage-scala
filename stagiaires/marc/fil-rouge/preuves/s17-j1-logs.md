# S17 J1 — Logs structurés et corrélation MDC

## Réalisation

Le runtime v3.2 utilise Logback 1.5.38 comme unique backend SLF4J. La
configuration `logback.xml` reste lisible en local. La configuration
`logback-json.xml` utilise Logstash Encoder 9.0 et ajoute les champs stables
`service=clearing-engine` et `environment`.

`ObservedDurableProcessor` enveloppe le vrai `DurableRecordProcessor` v3.1. Il
pose `txId`, `topic`, `partition` et `offset` dans le MDC, puis restaure le
contexte précédent dans un `finally`. Il ne logue ni payload, ni IBAN, ni
montant, ni objet client. Les statuts de log sont bornés à `success`,
`duplicate` et `failure`.

## Preuves automatisées

Commande exécutée sous Eclipse Temurin Java 21.0.6 :

```bash
sbt 'testOnly clearing.v32.ObservedDurableProcessorSpec \
  clearing.v32.JsonLoggingSpec clearing.v31.*'
```

Résultat : 41 tests réussis, 0 échec, 1 test Cassandra live annulé par défaut.

Les tests prouvent :

- l'enveloppement du processor durable réel avec ses projections mémoire;
- la publication initiale puis la détection du doublon;
- la restauration d'un MDC antérieur après succès et exception;
- l'isolation de deux IDs sur deux virtual threads concurrents;
- l'absence des valeurs sensibles injectées dans le payload parmi les messages
  et champs MDC;
- l'encodage d'une ligne JSON parseable contenant `@timestamp`, `level`,
  `message`, `service` et `environment` depuis l'encoder configuré par le vrai
  fichier XML.

L'extrait JSON de l'application live sera ajouté au gate S17, lorsque le runtime
v3.2 complet pourra être lancé avec Kafka et Cassandra.
