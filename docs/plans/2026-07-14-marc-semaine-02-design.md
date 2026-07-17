# Marc — Semaine 2 Design

## But

Marc fait évoluer le moteur `v0.1` vers une version `v0.2` capable de lire un
fichier CSV, d'ignorer proprement les lignes invalides, de router les
transactions et de calculer les positions derrière un contrat `trait`.

## Modèle retenu

La transaction de S2 reste volontairement un tuple :

```scala
type Transaction = (Int, String, String, BigDecimal, String)
```

Les champs représentent l'identifiant, l'émetteur, le bénéficiaire, le montant
et le type. Ce choix respecte le niveau du cours et rend visibles les limites
des tuples avant l'introduction des `case class` en S5.

Le parser accepte deux formats : le format final à cinq colonnes et l'exemple
pédagogique à trois colonnes `sender,receiver,amount`. Dans le second cas, il
utilise `id = 0` et `type = VIR`. Il retourne `Option[Transaction]` : `Some`
porte la ligne valide, `None` signale une ligne vide ou malformée. `parseLines`
affiche une erreur sur `stderr` et conserve les lignes valides.

## Composants

- `TransactionV2` génère des tuples riches et garantit un receiver différent.
- `ReportGenerator` calcule les flux bilatéraux, le top des montants et une
  matrice antisymétrique.
- `TransactionRouter` couvre chaque branche demandée par pattern matching.
- `CsvParser` nettoie, déstructure et convertit les lignes.
- `Transaction` sert de factory via `apply`.
- `ClearingProcessor` définit validation, calcul et rapport.
- `SimpleClearingProcessor with Logger` orchestre lecture, parsing et netting.

## Données et erreurs

Le cœur ne lève pas d'exception pour une mauvaise ligne utilisateur. Les
exceptions de conversion restent confinées dans le parser avec `toIntOption` et
`BigDecimal` protégé. Les erreurs de fichier restent techniques et remontent au
point d'entrée. Le test d'intégration utilise un vrai fichier CSV de ressources.

## Critère de sortie

`sbt clean test` doit conserver S1 et valider S2. `sbt run` doit lire le fichier
`transactions.csv`, signaler les lignes rejetées, afficher les positions et
prouver que leur somme vaut zéro.
