# TP Jour 3 : DSL & Extensions

**Durée :** ~4h | **Fil Rouge :** Créer un langage fluide pour le clearing

---

## Exercice 1 : Extensions de Validation (1h30)

1. Crée un fichier `Extensions.scala`.
2. Ajoute une extension à `BigDecimal` : `def isPositive: Boolean`.
3. Ajoute une extension à `String` : `def isValidBankCode: Boolean` (vérifie longueur 3 et majuscules).
4. Refactorise ton `Validator` pour qu'il utilise ces méthodes (ex: `if tx.amount.isPositive`).

---

## Exercice 2 : La Magie de toText (1h30)

1. Reprends ta Type Class `ClearingSerializable` et tes `given`.
2. Définis une extension générique `[T](item: T)(using ClearingSerializable[T])` qui ajoute la méthode `toJson`.
3. Dans ton application, convertis une liste de banques et une liste de transactions en faisant simplement `list.map(_.toJson)`.

---

## Exercice 3 : Enrichissement de Java (1h)

1. Ajoute une extension à `java.time.LocalDateTime` pour formatter la date en "JJ/MM".
2. Utilise-la pour générer des préfixes de fichiers de logs : `s"log_${now.toSimpleFormat}.txt"`.

**Livrable** : Code source utilisant les extensions pour créer une syntaxe fluide et lisible sur les types de base et les types métier.
