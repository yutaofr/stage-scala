# Preuve S15 J4 — At-least-once et replay

Date d'exécution : 14 juillet 2026

## Fenêtres de crash unitaires

`CrashReplaySpec` vérifie quatre fenêtres :

- publication échouée : aucun marquage et aucun offset committable;
- publication confirmée, même processus, commit absent : replay absorbé par le
  cache mémoire;
- redémarrage : cache perdu et résultat republié;
- crash entre l'accusé et le marquage : résultat republié au redémarrage.

Une mutation a déplacé volontairement le marquage avant la publication. Deux
tests ont échoué : le registre contenait l'ID après une publication échouée et
le scénario ack-versus-mark perdait le replay. Après restauration de l'ordre
`publish -> ack -> mark -> commit`, 13 tests J3/J4 ont réussi.

## Replay Kafka réel

Après le gate de 1 000 événements, le groupe `marc-v30-j5` avait un lag nul.
La partition 0 a été replacée de l'offset 605 à 604 pour représenter le dernier
offset non committé lors d'un crash :

```text
GROUP           TOPIC           PARTITION  NEW-OFFSET
marc-v30-j5     clearing-input  0          604
```

Un nouveau processus, donc un nouveau cache mémoire, a relu un record :

```text
CONSUMER_V30 published=1 duplicates=0 failedPartitions=0
```

Le total DLQ est passé de 100 à 101 et l'offset du groupe est revenu à 605 avec
lag 0. Le replay a donc produit un doublon observable. Cette preuve exclut toute
revendication d'exactly-once externe. La semaine 16 devra remplacer le cache
par un état Cassandra durable.
