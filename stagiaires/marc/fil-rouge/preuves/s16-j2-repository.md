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
