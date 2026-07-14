# Marc — Semaine 1 Design

## But

Marc construit lui-même la version `v0.1` du moteur de compensation. La semaine
doit lui faire pratiquer la syntaxe Scala 3, les types numériques, les
expressions, les fonctions et les premiers tests sans anticiper les ADT des
semaines suivantes.

## Approche retenue

Le projet cumulatif vit dans `stagiaires/marc/fil-rouge`. Chaque TP ajoute une
brique au même projet. Le code de production reste volontairement simple : les
transactions utilisent encore des tuples et des montants signés. Les limites de
ce modèle seront consignées dans la rétrospective pour préparer S2 à S5.

Les calculs financiers finaux utilisent `BigDecimal`. `Double` apparaît
uniquement dans `PrecisionDemo`, où Marc observe précisément pourquoi ce type ne
convient pas à l'argent. Les fonctions qui affichent un résultat s'appuient sur
des fonctions pures testables afin que la console ne soit pas l'unique preuve.

## Architecture S1

```text
TransactionGenerator
        |
        v
TransactionCategorizer -> TransactionFilter
        |                        |
        +------------------------+
                    |
                    v
             ClearingEngine
                    |
                    v
          NettingCalculator -> rapport console
```

`Validator`, `CurrencyConverter`, `InterestCalculator` et `TransactionSearch`
sont des exercices isolés mais restent dans le même package `clearing`.

## Erreurs et tests

S1 n'introduit pas encore `Either`. Les prédicats retournent des `Boolean`, la
validation globale retourne le message demandé par le TP, et la recherche
récursive retourne `Option`. Chaque fonction publique reçoit un test avant son
implémentation. Le test d'intégration vérifie le nombre de transactions, le
partitionnement valide/rejeté et l'invariant de netting global.

## Critère de sortie

Marc peut expliquer `val`, `BigDecimal`, expression, fonction et test. Depuis un
checkout propre, `sbt clean test` et `sbt run` doivent réussir. Le journal S1
doit relier chaque objectif aux fichiers et aux preuves produites.
