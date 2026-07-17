# Marc Semaine 12 — Conception du Clearing Engine v2.3

## Portée du stage

La semaine 12 reste dans le parcours. Elle enseigne les functors, les monades,
le désucrage des `for` et ScalaCheck avec la bibliothèque standard. Elle
n'introduit pas Cats. La semaine 13 livre une orchestration Pekko et la semaine
14 observe ZIO; le parcours de Marc les marquera donc comme ignorées par
contrat. La prochaine semaine implémentée après S12 sera S15, consacrée à
Kafka.

Le v2.3 prolonge le paquet `clearing.v22` au lieu de modifier son contrat. Il
réutilise le domaine opaque, le railway, les erreurs, le hash sûr et les trois
serializers. Le nouveau paquet `clearing.v23` ajoute seulement les abstractions
et preuves demandées par S12. Cette stratégie garde les jalons S1–S11
exécutables et relie chaque exercice au fil rouge réel.

## Functor et Monad sans Cats

`Functor[F[_]]` expose une extension `map`. Des instances `given` couvrent
`List`, `Option` et la case class pédagogique `Box[A]`. La fonction générique
`transform` ne connaît aucun conteneur concret. Les tests vérifient les exemples
du TP, puis les lois d'identité et de composition.

`Monad[F[_]]` étend `Functor[F]`, demande `pure` et `flatMap`, et dérive `map`
de ces deux opérations. `MonadicLogger[A]` transporte une valeur et un journal
immutable. Son `flatMap` concatène le journal courant avant le journal suivant;
ce choix préserve l'ordre chronologique. La fonction générique `chain` prouve
que l'orchestration dépend du contrat `Monad`, pas de la classe concrète.

Le logger n'acquiert aucun canal d'erreur caché. Les erreurs bloquantes restent
des `Either` du railway v2.2. Un petit laboratoire nominal montre comment le
logger accumule les étapes; un test de compilation prouve qu'un `Option` et un
`Either` ne se lient pas directement dans le même `for`.

## Pipeline v2.3

`V23Pipeline.runPure` emploie un seul `for-yield` sur `MonadicLogger`. Les
étapes observent l'entrée, exécutent `TypedRailwayEngine`, recomputent le
netting pur, puis exportent le résultat avec le scope JSON, CSV ou XML. Le
journal contient des faits stables : nombre de lignes, succès, rejets, somme
globale et format. Il ne contient ni IBAN, ni hash, ni ligne CSV brute.

`ClearingAppV23` garde la lecture de fichier et la console aux bords. La lecture
retourne un `Either`; le cœur retourne un `MonadicLogger[V23Execution]`.
`V23Reporter` rend le rapport puis le journal. Cette séparation montre les deux
contextes sans inventer de monad transformer, sujet exclu du programme.

## Propriétés et générateurs

ScalaCheck construit directement des valeurs valides. Le générateur choisit
une paire de banques distinctes dans une liste finie, produit des centimes
strictement positifs, puis crée `Money` et `PreparedTransaction`. Il n'utilise
aucun `Gen.filter`; ScalaCheck ne peut donc pas abandonner à cause d'un excès
de candidats rejetés.

Les propriétés couvrent les lois de Functor, les trois lois de Monad, l'égalité
entre `for-yield` et son désucrage, l'égalité de chaque position v2.3 avec un
oracle indépendant et l'invariant financier. Deux propriétés exécutent chacune
10 000 batchs de 200 transactions : l'une vérifie la somme globale nulle;
l'autre compare les crédits reçus moins les débits émis pour chaque banque. Les
montants partent de centimes entiers afin d'éviter tout passage par `Double`.

## Démonstration et acceptation

La démonstration v2.3 traite le fichier de référence dans les trois formats et
montre le journal pur. Le scénario nominal conserve trois succès, quatre rejets
et une somme globale nulle. Le scénario de panne de hash conserve une erreur
technique isolée. Un fichier absent reste une erreur technique stable.

L'acceptation exige les exercices J1–J5, les preuves de compilation, les 10 000
batchs ScalaCheck, la non-régression S1–S12 sous Java 21 et Java 17, le gate de
couverture v2.0, les démonstrations, l'audit de pureté et la revue mentor. Le
suivi hebdomadaire et la rétrospective distingueront les propriétés testées
d'une preuve mathématique formelle : ScalaCheck augmente fortement la confiance,
mais n'explore pas toutes les valeurs possibles.
