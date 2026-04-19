# Semaine 7 : Logique Business de Compensation (5 jours)

## Jour 1 — GroupBy & MapValues sur ADTs
**Cours (2h)** : `groupBy` pour segmenter des listes d'ADTs. `view.mapValues` pour transformer les groupes. Application : grouper les transactions par paire (sender, receiver).
**TP (4h)** : Calculer la matrice bilatérale de compensation (chaque paire de banques) en utilisant les ADTs typés.

## Jour 2 — Algorithme de Compensation Multilatérale
**Cours (2h)** : Théorie du netting multilatéral. Différence entre netting bilatéral et multilatéral. Le théorème : "La somme des positions nettes est toujours 0".
**TP (4h)** : Implémenter `def multilateralNetting(txs: List[Transaction]): Map[Bank, BigDecimal]`. Prouver par test que la somme = 0.

## Jour 3 — SortBy, Partition, Sliding
**Cours (2h)** : Tri par critères multiples. `partition` (split en deux listes). `sliding` et `grouped` pour le batch processing.
**TP (4h)** : Découper le fichier de transactions en batches de 1000. Traiter chaque batch indépendamment. Fusionner les résultats.

## Jour 4 — Performance & Benchmarking
**Cours (2h)** : Complexité algorithmique des opérations de collection. `Vector` vs `List` pour l'accès aléatoire. Mesure de performance simple.
**TP (4h)** : Benchmark avec 1 million de transactions. Identifier les goulots. Optimiser le pipeline.

## Jour 5 — Clearing Engine v1.2
**Cours (2h)** : Revue de l'architecture du moteur v1.2.
**TP (4h)** : Le moteur calcule le netting multilatéral complet pour N banques. Rapport détaillé par paire + rapport agrégé. Tests de propriétés : "la somme nette = 0".
