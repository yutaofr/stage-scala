# TP Jour 2 : Refactoring Anti-Null

**Durée :** ~4h | **Fil Rouge :** Assainir le code du moteur

---

## Exercice 1 : Recherche Sécurisée (1h)

1. Modifie ton `repertoire` de banques (Jour 1 - S3).
2. Crée une fonction `findBankName(code: String): Option[String]`.
3. Teste-la avec un code existant et un code inexistant.
4. Utilise `getOrElse` pour afficher le résultat proprement.

---

## Exercice 2 : Chainage d'Options (1h30)

1. Tu as deux Maps :
   - `accounts: Map[String, Int]` (IBAN -> UserID)
   - `users: Map[Int, String]` (UserID -> UserName)
2. Crée une fonction `findUserNameByIban(iban: String): Option[String]`.
3. Utilise `flatMap` pour faire le lien.
4. Si l'IBAN n'existe pas OU si le UserID n'a pas de nom, le résultat doit être `None`.

---

## Exercice 3 : Plus de chaînes vides (1h)

1. Crée une fonction `cleanInput(s: String): Option[String]`.
2. Elle doit retourner `None` si la chaîne est vide ou ne contient que des espaces.
3. Sinon, elle retourne `Some(s.trim)`.
4. Utilise `map` pour transformer le résultat en majuscules.

---

## Exercice 4 : Option dans les Listes (30 min)

1. Tu as une `List[Option[BigDecimal]]`.
2. Utilise `flatten` pour obtenir une `List[BigDecimal]` ne contenant que les valeurs présentes.
3. Calcule la somme de ces valeurs.

**Livrable** : Code refactorisé utilisant `Option` partout à la place de `null` ou de valeurs par défaut arbitraires.
