# Conception — Marc, semaine 4

## Résultat attendu

Marc livre le Clearing Engine `v0.4`, prototype final du premier mois. Le
moteur lit un CSV, compose parsing, validation et enrichissement avec `Option`,
applique une liste de règles fournie par l'appelant, calcule le netting avec
`foldLeft` et rend un rapport trié.

## Approches étudiées

### Un validateur monolithique

Une fonction contenant toutes les conditions serait courte. Elle ne montrerait
ni HOF, ni configuration partielle, et chaque nouvelle règle imposerait une
modification du validateur.

### Une table de règles nommées par des chaînes

Une `Map[String, Boolean]` rendrait les règles configurables en apparence. Elle
perdrait le typage du comportement et déplacerait les fautes vers l'exécution.

### Des fonctions comme règles

Une règle est un `BigDecimal => Boolean`. `forall` et `exists` composent les
règles, et l'appelant choisit sa liste. Cette solution couvre directement HOF,
currying et fonctions spécialisées. Elle constitue l'architecture retenue.

## Progression pédagogique

| Jour | Notions | Exercice intégré | Preuve attendue |
|---|---|---|---|
| J1 | HOF, currying, `forall`, `exists` | Filtres, frais et moteur de règles | Tests avec plusieurs critères et banques |
| J2 | `Option`, `map`, `flatMap`, `flatten` | Répertoire, comptes et entrées propres | Présence, absence et chaîne interrompue |
| J3 | Match et for-comprehension sur `Option` | Parser, change et workflow complet | Lignes valides retenues, rejets comptés |
| J4 | `@tailrec`, accumulateur, arrêt précoce | Historique, incident, signature et Fibonacci | Gros volume sans débordement de pile |
| J5 | Intégration du mois 1 | `MainV04` configurable | CSV mixte, rapport correct et non-régression |

## Modules

### `ValidationRules`

Le module expose l'alias `Rule`, les trois règles du TP, les combinateurs
`validateAll` et `validateAny`, un filtre recevant une fonction et le
configurateur curryfié de frais. Le calcul de frais reste un exercice isolé :
le netting ne l'applique pas, car une commission unilatérale casserait
l'invariant global nul.

### `OptionTools` et `ReferenceData`

`ReferenceData.findBankName` retourne `Option[String]`. `OptionTools` relie
IBAN, utilisateur et nom avec `flatMap`, nettoie les chaînes, aplatit les
montants présents et convertit une devise par recherche sûre dans une `Map`.

### `TransactionWorkflowV4`

Le workflow sépare trois fonctions totales : parser, valider et enrichir. Une
for-comprehension les compose. Une seule étape renvoyant `None` interrompt le
traitement de la ligne. La collecte utilise `view`, `map` et `flatten`, puis
retourne les transactions acceptées et le nombre de lignes ignorées.

### `BankingRecursion`

Les quatre algorithmes emploient une boucle interne annotée `@tailrec`.
L'incident correspond au premier solde strictement inférieur à `-500` et son
index commence à zéro, conformément aux index Scala.

### `MainV04`

Le point d'entrée lit le fichier, délègue au workflow, calcule les positions et
affiche les compteurs. Le résultat reste un tuple nommé par un alias. Les
`case class`, enums et erreurs riches arriveront en S5 et S6.

## Flux de données

```text
CSV -> List[String] -> view -> Transaction.apply
    -> Option validation -> Option enrichissement -> flatten
    -> List[Transaction] -> NettingCalculator.calculate
    -> Map[banque, position] -> rapport trié
```

## Critères d'acceptation mentor

1. Les trois critères de filtrage, les deux taux de frais et les combinateurs
   `forall`/`exists` possèdent des tests.
2. Chaque recherche absente retourne `None`; aucune API S4 ne retourne `null`
   ou une chaîne vide comme sentinelle.
3. Le workflow compose parser, validation et enrichissement dans une
   for-comprehension et compte chaque ligne ignorée.
4. Les fonctions récursives portent `@tailrec`; l'historique et le premier
   incident sont exacts sur les frontières.
5. Le CSV de démonstration mélange dix transactions valides et six rejets. Le
   moteur conserve les positions attendues et un total global nul.
6. Une liste de règles différente modifie réellement l'acceptation sans changer
   le workflow.
7. Les tests S1 à S4 passent avec Java 21 local et Java 17 Docker.
8. Le code de production ne contient aucun `var`, `null`, `???`, `while` ou
   collection mutable. `Double` reste limité à `PrecisionDemo`.
