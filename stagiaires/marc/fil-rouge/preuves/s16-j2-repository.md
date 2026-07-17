# Preuve S16 J2 — Repository asynchrone

Date : 14 juillet 2026

Le port `DurableRepository` retourne uniquement des `CompletionStage`. Le fake
mémoire emploie les mêmes clés que le schéma Cassandra et permet de tester la
reprise sans conteneur.

## Invariants couverts

- plusieurs fingerprints peuvent appartenir au même `event_key` ;
- une transition met à jour une seule ligne d'état ;
- un événement validé produit une ligne d'historique, deux mouvements, deux
  contributions de position et une activité de paire ;
- rejouer le même événement conserve exactement le même nombre de lignes ;
- la position sender est négative, la position receiver positive et leur somme
  vaut zéro ;
- la lecture historique fusionne les buckets et trie par date décroissante ;
- le top des paires agrège des contributions idempotentes.

## Cycle TDD

```bash
sbt 'testOnly clearing.v31.DurableRepositorySpec'
```

Le premier run a échoué à la compilation sur les types v3.1 absents. Le run
GREEN vérifie le repository et le bucket sur mille clés déterministes.

Le même contrat a ensuite été exécuté contre Cassandra 4.1.11 avec le driver
Apache 4.19.3 :

```text
CassandraRepositoryIntegrationSpec
Tests: succeeded 1, failed 0
```

Le test tronque les cinq tables, écrit deux fois le même événement via
`executeAsync`, puis relit une seule ligne d'état, une seule ligne historique,
une contribution par banque et une activité de paire.

## Comparaison séquentielle / parallèle

Le laboratoire `benchmark` utilise 100 projections déterministes. Le warm-up
parcourt les 100 clés dans les deux modes; les six mesures portent donc toutes
sur des upserts de la même distribution. Il exécute ensuite trois mesures
séquentielles puis trois mesures sur un pool fixe de huit threads. Chaque
mesure conserve durée, débit, succès et erreurs; elle ne sélectionne pas
seulement le meilleur run.

```bash
sbt 'run benchmark --samples 100 --repetitions 3 --parallelism 8'
```

| Mode | Run | Durée (ms) | Débit (records/s) | Succès | Erreurs |
|---|---:|---:|---:|---:|---:|
| Séquentiel | 1 | 81.59 | 1225.71 | 100 | 0 |
| Séquentiel | 2 | 74.42 | 1343.66 | 100 | 0 |
| Séquentiel | 3 | 71.82 | 1392.33 | 100 | 0 |
| Parallèle, 8 threads | 1 | 15.77 | 6342.80 | 100 | 0 |
| Parallèle, 8 threads | 2 | 14.85 | 6732.42 | 100 | 0 |
| Parallèle, 8 threads | 3 | 15.51 | 6446.78 | 100 | 0 |

Sur cette machine et ce jeu local, la variante parallèle est plus rapide dans
les trois runs. Ce résultat décrit uniquement ce laboratoire : il ne fixe pas
un débit de production et ne justifie pas d'augmenter la concurrence sans
mesurer la saturation Cassandra.
