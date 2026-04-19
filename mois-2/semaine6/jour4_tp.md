# TP Jour 4 : Factory Methods & Compagnons

**Durée :** ~4h | **Fil Rouge :** Centraliser et sécuriser la création des transactions

---

## Exercice 1 : Le Compagnon Intelligent (1h30)

1. Dans `Transaction.scala`, implémente la méthode `fromCsv(line: String): Option[Transaction]`.
2. Elle doit gérer les erreurs de format (trop peu de colonnes) et les erreurs de typage (montant non numérique).
3. Utilise `scala.util.Try` pour capturer les exceptions de parsing et les transformer en `Option`.

---

## Exercice 2 : Factory de devises (1h)

1. Crée un objet compagnon pour ton enum `Currency`.
2. Ajoute une méthode `fromString(code: String): Currency`.
3. Elle doit être insensible à la casse (ex: "eur" -> `Currency.EUR`).
4. Si le code est inconnu, retourne une valeur par défaut ou une `Option`.

---

## Exercice 3 : Constructeurs Privés (1h30)

1. Crée une `case class Iban private (value: String)`.
2. Dans son compagnon, crée une méthode `apply(raw: String): Option[Iban]`.
3. Règle : un IBAN doit faire 24 caractères et commencer par "MA".
4. Vérifie que tu ne peux plus instancier `Iban("abc")` directement depuis une autre classe.

**Livrable** : Code source montrant l'utilisation des compagnons comme uniques points d'entrée pour la création d'objets valides.
