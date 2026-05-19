# Semaine 9 : Pureté & Composition (5 jours)

## Jour 1 — Transparence Référentielle
**Cours (2h)** : Définition mathématique de la pureté. Une fonction est pure si on peut remplacer son appel par sa valeur retournée sans changer le programme. Effets de bord cachés : `println`, `var`, `Random`, `System.currentTimeMillis`.
**TP (4h)** : Audit du moteur v1.3 : identifier et isoler chaque effet de bord. Créer un module `PureClearingEngine` qui ne fait que du calcul, et un module `IOBridge` qui gère les I/O.

## Jour 2 — Fonctions Pures & Immuabilité Totale
**Cours (2h)** : Avantages de la pureté : testabilité, parallélisabilité, raisonnement local. Techniques : retourner les résultats au lieu de modifier un état. Exemple : `def process(txs: List[Transaction]): ClearingResult` au lieu de `def process(): Unit`.
**TP (4h)** : Réécrire le `NettingCalculator` pour qu'il soit 100% pur : aucun `println`, retourne un `ClearingReport` au lieu d'afficher. Tous les tests passent identiquement.

## Jour 3 — Composition de Fonctions
**Cours (2h)** : `andThen` : chaîner A→B puis B→C. `compose` : chaîner B→C puis A→B. Construire un pipeline comme une composition de micro-transformations. Avantage : chaque brique est testable indépendamment.
**TP (4h)** : Créer les 4 fonctions pures du pipeline : `parse: String => List[RawTransaction]`, `validate: List[RawTransaction] => List[Transaction]`, `calculate: List[Transaction] => ClearingResult`, `format: ClearingResult => String`. Les composer avec `andThen`.

## Jour 4 — Currying Avancé
**Cours (2h)** : Currying : transformer `(A, B) => C` en `A => B => C`. Application partielle : pré-remplir la configuration. Cas d'usage : `def applyFees(config: FeeConfig)(tx: Transaction): Transaction`.
**TP (4h)** : Pré-configurer le moteur (devise de référence, taux de frais) via currying. Créer deux instances : `clearingMAD` et `clearingEUR` à partir de la même logique.

## Jour 5 — Clearing Engine v2.0
**Cours (2h)** : Revue. Comment la pureté simplifie les tests et prépare le terrain pour la concurrence (Mois 4).
**TP (4h)** : Assemblage final. Le moteur v2.0 est un pipeline de fonctions composées. Zéro effet de bord visible dans la logique métier. Tests : 100% de couverture sur le noyau pur.
