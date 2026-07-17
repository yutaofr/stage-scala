# Rétrospective S12 — Monades et tests de propriétés

## Auto-évaluation

| Concept | Confiance (1-5) | Point d'amélioration |
|---|---:|---|
| Functor (`map`) | 4 | Reconnaître les lois dans une abstraction métier moins évidente qu'un conteneur |
| Monad (`flatMap + pure`) | 4 | Comparer plus de stratégies d'empilement des contextes |
| For-Comprehension | 5 | Garder visible le `map` final dans toute démonstration de désucrage |
| ScalaCheck (`Gen`, `forAll`) | 4 | Étudier la distribution, les labels et la couverture des cas générés |
| Opaque Types | 5 | Modéliser aussi les taux et pourcentages par des types distincts |
| Type Classes (`given`) | 4 | Tester systématiquement l'instance résolue, pas seulement le type concret |

## Comparaison Mois 1 / Mois 3

Au Mois 1, le netting recevait des tuples et mettait à jour une
`Map[String, BigDecimal]`. Le test vérifiait quelques exemples choisis. Au Mois
3, le chemin actif reçoit des `PreparedTransaction` avec `BankCode` et `Money`
opaques. `V23Netting` reste une fonction pure, mais ScalaCheck compare désormais
chaque position à un oracle indépendant sur 10 000 batchs et vérifie aussi la
conservation globale. Le code est plus explicite sur ses types; les propriétés
protègent des classes de défauts que les exemples seuls ne couvraient pas.

## 1. Pourquoi `map` se dérive de `flatMap + pure`

`flatMap` ouvre le contexte `F[A]` et attend une fonction qui retourne un
nouveau `F[B]`. Une fonction ordinaire `A => B` ne fournit pas ce contexte;
`pure` replace son résultat dans `F`. La définition est donc
`flatMap(fa)(a => pure(f(a)))`. `Monad` gagne ainsi le comportement de
`Functor` sans une seconde implémentation indépendante.

## 2. Ce que `MonadicLogger` fait et ne fait pas

`MonadicLogger.flatMap` concatène `logs ++ next.logs`. Il accumule les traces
dans l'ordre et ne produit aucun effet de bord. Il ne représente cependant pas
un échec : sa valeur peut être un `Either`, mais le logger continue son propre
enchaînement même si cet `Either` vaut `Left`. Le laboratoire rend cette limite
visible avec les traces « validation ignorée » et « sauvegarde ignorée ».

## 3. Désucrage du `for`

Les générateurs successifs d'un `for` deviennent des appels imbriqués à
`flatMap`; le dernier `yield` devient un `map`. `ForEquivalence` conserve cette
forme littérale, y compris l'identity `map`, pour le railway et le logger. Les
tests comparent leurs rails droits et gauches. La preuve de compilation négative
montre aussi qu'un `Either` ne sait pas enchaîner directement une étape qui
retourne `Option`.

## 4. Pourquoi générer au lieu de filtrer

Un filtre demande à ScalaCheck de produire des candidats puis d'en jeter une
partie. Si la condition est rare, la propriété peut abandonner avant d'obtenir
assez d'essais. Le générateur choisit donc directement l'un des six couples de
banques distinctes et construit le montant à partir de centimes entiers. Les
10 000 batchs réussis ne dépendent d'aucun taux de rejet caché.

## 5. Le défaut trouvé pendant le cycle TDD

Le générateur principal excluait les auto-virements, conformément aux données
métier valides. Le contrat du calculateur exigeait pourtant qu'ils s'annulent.
Le premier fold calculait débit et crédit depuis la même map initiale; la mise à
jour du crédit écrasait alors le débit. Une régression a observé `+125.75` au
lieu de zéro. Le calcul applique maintenant les deux ajustements l'un après
l'autre.

## 6. Limite de la certification

Deux propriétés exercent chacune 10 000 batchs de 200 transactions. La première
vérifie la conservation globale; la seconde compare chaque banque aux crédits
reçus moins les débits émis, calculés par un oracle indépendant. Ces essais ne
démontrent pas le résultat pour l'ensemble infini des montants et des listes.
ScalaCheck cherche des contre-exemples; il ne remplace pas une preuve
mathématique ni une vérification formelle.

## 7. Bilan avant validation finale

Le cœur v2.3 compose le railway typé, le netting vérifié par propriétés et les
type classes de sérialisation dans un seul `for-yield` pur. Le rapport et les
logs restent des valeurs. Les gates Java 21, Java 17 Docker, couverture,
démonstrations et audit statique passent. Après correction de deux points
importants, la revue senior finale ne conserve aucun point critique ou
important; S12 est validée.
