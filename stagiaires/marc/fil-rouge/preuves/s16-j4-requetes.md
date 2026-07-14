# Preuve S16 J4 — Kafka durable et requêtes

Date : 14 juillet 2026

## Frontière Kafka

Le consumer v3.1 conserve `enable.auto.commit=false` et fixe
`max.poll.records=1`. Cette borne limite à un le nombre de graphes
`CompletionStage` actifs dans la boucle pédagogique.

Le test `MockConsumer` couvre deux polls : une panne au second record committe
le premier offset terminé, fait `seek` sur le second, puis traite le second et
le troisième. Un troisième poll ne republie rien.

La fermeture libère dans l'ordre consumer, producer et session Cassandra. Si
une fermeture échoue, les suivantes sont tout de même tentées et les erreurs
secondaires sont ajoutées comme suppressed exceptions.

## Requêtes

- `report --bank AWB --date 2026-07-14` lit une partition de mouvements et une
  partition de contributions de position ;
- l'historique lit explicitement les 16 buckets ;
- le top des paires lit la partition du jour et trie dans Scala ;
- fake et live départagent un timestamp identique par `eventKey` ;
- le test live force `pageSize=1` pour exercer `fetchNextPage`.

Le parcours des pages utilise un accumulateur inversé puis un seul `reverse`.
Il évite la concaténation quadratique d'une liste complète à chaque page.

## Réconciliation de position

`report` lit la partition complète `transactions_by_bank_day` de la banque et
du jour, applique `IN = crédit` et `OUT = débit`, puis compare cette position à
`bank_positions`. Le run réel après le laboratoire benchmark a donné :

```text
REPORT_V31 bank=AWB date=2026-07-14 position=-190.00 txCount=102
movementPosition=-190.00 positionsMatch=true
```

Le test fake et le test Cassandra live protègent la même égalité. L'invariant
global reste contrôlé séparément, car l'égalité d'une banque ne suffit pas à
prouver l'équilibre de toutes les banques.

## Simulation dashboard

```bash
sbt 'run dashboard --banks AWB,CIH --date 2026-07-14 \
  --interval-seconds 5 --refreshes 3'
```

Le programme a rendu trois rafraîchissements espacés de cinq secondes pour les
deux banques. Chaque ligne contient l'heure d'observation, le timestamp de la
dernière donnée et son âge. Un seul job planifié dessert la liste bornée. Un
test utilise volontairement un pool planifié de deux threads, un intervalle
d'une nanoseconde et un chargement asynchrone plus long : l'in-flight guard
conserve un maximum observé de un chargement et exactement trois appels, puis
le scheduler s'arrête après le troisième résultat.
