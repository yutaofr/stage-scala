# Preuve S15 J3 — Consumer et commits manuels

Date d'exécution : 14 juillet 2026

Le producer v3.0 avait déposé 50 événements typés après le message console du
J1. Le consumer a été lancé avec un nouveau groupe :

```bash
sbt 'runMain clearing.v30.runKafkaConsumerV30 \
  --group-id marc-v30-j3 --max-records 51'
```

Résultat :

```text
CONSUMER_V30 published=51 duplicates=0 failedPartitions=0
clearing-output: 50 records
clearing-dlq:     1 record
```

Le record J1 avait volontairement un schéma différent et a donc rejoint la
DLQ. Les 50 événements produits par l'application ont rejoint la sortie.

Après fermeture du consumer, le groupe ne présentait aucun lag :

```text
PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG
0          32              32              0
1          8               8               0
2          11              11              0
```

Ces valeurs prouvent que le consumer committe l'offset suivant pour chaque
partition. Les tests unitaires vérifient séparément que publication, accusé,
marquage et commit sont exécutés dans cet ordre.

