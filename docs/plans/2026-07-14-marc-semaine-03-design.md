# Conception — Marc, semaine 3

## Résultat attendu

Marc livre le Clearing Engine `v0.3`. Le programme lit un CSV, parse les
lignes, élimine les transactions impropres au clearing, calcule les positions
avec `foldLeft` et affiche un rapport trié. Toutes les collections restent
immuables.

## Progression pédagogique

| Jour | Notions | Exercice intégré | Preuve attendue |
|---|---|---|---|
| J1 | `List`, `Map`, `Set`, `groupBy` | Répertoire, IBANs uniques et index par émetteur | Tests de recherche, unicité et comptage |
| J2 | `map`, `filter`, `flatMap` | Enrichissement, filtrage et éclatement de lots | Pipeline sans boucle impérative |
| J3 | `reduce`, `foldLeft`, agrégations | Somme, moyenne, positions et statistiques | Liste vide sûre et somme des positions nulle |
| J4 | `List`, `Vector`, `view` | Mesure sur 100 000 transactions | Résultats eager/lazy identiques et rapport mesuré |
| J5 | Intégration fonctionnelle | `MainV03` de CSV au rapport | Scénario de dix transactions et non-régression |

## Modules

### `ReferenceData`

Le module contient les cinq banques demandées, leurs codes pédagogiques et les
codes courts du moteur. Il expose une recherche avec valeur par défaut, la
déduplication d'IBANs par `Set` et l'indexation des transactions par `groupBy`.

### `TransactionPipelineV3`

Le module transforme les tuples existants. Il enrichit les codes avec les noms
de banque, filtre les montants non positifs, les banques inconnues et les
virements internes, puis aplatit les lots avec `flatMap`. Les tuples restent
volontaires : Marc étudiera les `case class` et les ADT en S5.

### `NettingCalculator`

Le calculateur conserve les exercices de S1 et ajoute :

- une somme et un compteur calculés en un passage ;
- une moyenne sûre sur une collection vide ;
- les positions nettes construites avec un seul `foldLeft` ;
- les statistiques d'envoi produites après `groupBy` ;
- l'invariant de compensation, somme des positions égale à zéro.

### `PerformanceLab`

Le laboratoire compare `List`, `Vector`, pipeline strict et pipeline avec
`view`. Il mesure avec `System.currentTimeMillis()` comme le demande le TP. Les
tests comparent les résultats, jamais un seuil de temps dépendant de la machine.
La démonstration exécute explicitement 100 000 transactions.

### `MainV03`

`MainV03` orchestre cinq étapes visibles : lecture, parsing, filtrage, calcul et
rapport. Le rapport trie les banques de A à Z et affiche le solde global. Le
programme principal de S2 reste disponible pour les tests de non-régression.

## Règles de conception

- Employer `BigDecimal` pour chaque montant.
- Employer des collections immuables et des `val`.
- Réserver les effets de lecture, de mesure et d'affichage aux bords.
- Garder le calcul et les transformations purs.
- Accepter les collections vides sans exception.
- Ne pas introduire de modèle métier en avance sur S5.

## Critères d'acceptation mentor

1. Le répertoire contient au moins cinq banques et la recherche inconnue répond
   `Banque Inconnue`.
2. Les exemples montrent réellement `Set`, `groupBy`, `map`, `filter`,
   `flatMap`, `foldLeft`, `Vector` et `view`.
3. Le netting débite l'émetteur, crédite le bénéficiaire et conserve un total
   global nul, y compris sur une liste vide.
4. Le laboratoire traite 100 000 transactions et produit des résultats
   fonctionnellement identiques avec les variantes comparées.
5. `MainV03` traite un CSV de dix transactions et trie son rapport.
6. Les tests S1 et S2 restent verts.
7. Le code de production ne contient ni `var`, ni `null`, ni `???`.
8. `sbt clean test` passe avec Java 21 local et Java 17 dans l'image Docker du
   cours.
