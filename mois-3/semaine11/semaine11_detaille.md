# Semaine 11 : Polymorphisme Ad-hoc (5 jours)

## Jour 1 — Introduction aux Type Classes
**Cours (2h)** : Le problème : comment ajouter un comportement (`toJson`) à un type qu'on ne possède pas (`Transaction`) sans modifier sa source ? Réponse : le polymorphisme ad-hoc via les Type Classes. Pattern : `trait Serializer[T] { def serialize(t: T): String }`.
**TP (4h)** : Créer `trait JsonSerializer[T]` et implémenter pour `Transaction`, `Bank`, `ClearingResult`. Créer `trait CsvSerializer[T]` avec les mêmes types.

## Jour 2 — Context Parameters (using / given)
**Cours (2h)** : `given` : fournir une instance de Type Class. `using` : la demander en paramètre. Le compilateur résout automatiquement l'instance. Scope de résolution.
**TP (4h)** : Écrire `def export[T](data: T)(using serializer: Serializer[T]): String`. Injecter le bon serializer via les `given`. Exporter les résultats en JSON et CSV sans changer la fonction.

## Jour 3 — Extension Methods
**Cours (2h)** : Syntaxe `extension (t: Transaction) def toJson: String`. Enrichir les types existants sans héritage. Créer un DSL métier lisible.
**TP (4h)** : Ajouter `.toJson`, `.toCsv`, `.isValid`, `.toSummary` aux types de domaine. Pouvoir écrire `clearingResult.toJson` directement.

## Jour 4 — Opaque Types
**Cours (2h)** : Le problème du "Primitive Obsession" : `String` pour l'IBAN, `String` pour le nom de banque, `BigDecimal` pour le montant... trop facile de les mélanger. Solution : `opaque type Iban = String`. Zéro overhead runtime, sécurité à la compilation.
**TP (4h)** : Créer `opaque type Iban`, `opaque type Money`, `opaque type BankId`. Prouver que `val x: Iban = someString` ne compile pas sans passer par le constructeur validant.

## Jour 5 — Clearing Engine v2.2
**Cours (2h)** : Revue. Comment les abstractions rendent le code plus sûr ET plus flexible.
**TP (4h)** : Le moteur v2.2 utilise des types opaques partout, exporte en JSON et CSV via le polymorphisme ad-hoc. Le même code de sérialisation fonctionne pour tous les types du domaine.
