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
