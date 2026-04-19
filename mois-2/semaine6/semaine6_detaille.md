# Semaine 6 : Validation Avancée & Pattern Matching Exhaustif (5 jours)

## Jour 1 — Sealed Traits Imbriqués
**Cours (2h)** : Hiérarchies d'erreurs riches : `ClearingError` → `ValidationError` / `BusinessError` / `SystemError`. Chacun avec des sous-cas. Pattern matching multi-niveaux.
**TP (4h)** : Modéliser les 10 codes d'erreur ISO 20022 les plus fréquents comme des ADTs. Créer un formatter qui produit des messages lisibles.

## Jour 2 — Gardes et Extracteurs Personnalisés
**Cours (2h)** : Gardes conditionnelles dans le `match`. Création d'extracteurs custom avec `unapply`. Exemple : `IbanExtractor` qui décompose un IBAN en (pays, checksum, banque, compte).
**TP (4h)** : Implémenter `object Iban { def unapply(s: String): Option[(String, String, String)] }`. Utiliser cet extracteur dans le pipeline de validation.

## Jour 3 — Pattern Matching + For-Yield
**Cours (2h)** : Combiner des for-comprehensions avec du pattern matching pour filtrer et transformer simultanément.
**TP (4h)** : Pipeline de validation qui accumule les résultats : passer de `List[Transaction]` à `List[(Transaction, List[ClearingError])]`.

## Jour 4 — Companion Objects & Factory avancées
**Cours (2h)** : Le pattern Factory Method via `apply` dans les companion objects. Validation à la construction.
**TP (4h)** : `Transaction.fromCsv(line: String): Option[Transaction]` qui intègre le parsing ET la validation en un seul appel.

## Jour 5 — Clearing Engine v1.1
**Cours (2h)** : Revue du concept de "Parse, Don't Validate" (Alexis King).
**TP (4h)** : Le moteur v1.1 rapporte toutes les erreurs d'un batch (pas seulement la première). Démonstration avec un fichier contenant des erreurs variées.
