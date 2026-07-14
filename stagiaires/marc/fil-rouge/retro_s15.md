# Rétrospective S15 — Kafka et at-least-once

## Auto-évaluation

| Concept | Confiance (1-5) | Point d'amélioration |
|---|---:|---|
| KRaft et topics | 4 | Tester un cluster multi-broker et la perte d'un leader |
| Producer idempotent | 4 | Étudier les transactions Kafka et leurs limites externes |
| Consumer groups | 4 | Observer davantage de rebalances et d'assignations |
| Commit manuel | 5 | Conserver la discipline offset suivant par partition |
| At-least-once | 4 | Remplacer le cache mémoire par un état durable en S16 |
| Docker Compose | 4 | Ajouter sécurité et limites de ressources hors laboratoire |

## Ce qui a changé depuis v2.3

Le v2.3 lisait un fichier entier avant de retourner un résultat. Le v3.0 reçoit
des records partitionnés et doit décider quand un record est réellement
terminé. Le calcul métier reste le même; la difficulté nouvelle se situe dans
l'ordre des effets et dans la reprise après crash.

## Pourquoi l'offset vaut `dernier + 1`

Kafka interprète l'offset committé comme la prochaine position à lire. Après
avoir traité l'offset 10, le consumer committe donc 11. Commettre 10 rejouerait
le record déjà terminé; committer 12 sauterait un record non traité.

## Pourquoi publier avant de committer

Si le commit précédait la publication et que le processus tombait entre les
deux, Kafka considérerait le record terminé alors que le résultat n'existe pas.
L'ordre choisi préfère un doublon possible à une perte silencieuse : traiter,
publier, attendre l'ack, marquer, puis committer.

## Limite du cache mémoire

Le cache évite un doublon seulement dans le même processus. Le replay Docker a
reculé un offset d'une position; le nouveau processus a republié le record et
fait passer la DLQ de 100 à 101. Ce comportement est conforme à at-least-once,
mais il interdit de parler d'exactly-once. Cassandra portera l'état durable en
S16.

## Kafka comparé au fichier et à HTTP

Un fichier fournit un lot simple et reproductible, mais peu de coordination
entre consommateurs. HTTP donne une réponse immédiate au demandeur, mais le
couple client-serveur doit gérer les indisponibilités synchrones. Kafka conserve
un journal, partitionne, distribue par groupe et permet le replay. En échange,
il ajoute offsets, rebalances, duplication possible, exploitation du broker et
contrats de schéma.

## Bilan

Le gate propre a produit et consommé 1 000 événements : 900 sorties, 100 DLQ,
lag nul et aucun IBAN brut. Le netting des succès reste équilibré. La mutation
du mauvais ordre mark/publish a bien été détectée. Le v3.0 est prêt pour la
revue mentor et pour la persistance durable de S16.
