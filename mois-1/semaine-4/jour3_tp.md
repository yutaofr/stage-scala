# TP Jour 3 : Parser de Transactions Résilient

**Durée :** ~4h | **Fil Rouge :** Un moteur qui encaisse tous les formats de fichiers

---

## Exercice 1 : Safe Map Lookup (1h)

1. Crée une `Map[String, BigDecimal]` de taux de change (EUR, USD, GBP).
2. Crée une fonction `convert(amount: BigDecimal, currency: String): Option[BigDecimal]`.
3. Elle doit utiliser `.get(currency)` pour récupérer le taux.
4. Si la devise est inconnue, elle retourne `None`.

---

## Exercice 2 : Le Parser qui ne meurt jamais (2h)

1. Reprends ton `CsvParser` du Mois 1 - Semaine 2.
2. Modifie `parseLine` pour qu'il retourne `Option[Transaction]`.
3. Utilise le Pattern Matching pour gérer :
   - Les lignes avec trop peu ou trop de colonnes.
   - Les colonnes vides.
   - Les montants non numériques (indice : utilise `Try(BigDecimal(s)).toOption`).

---

## Exercice 3 : Composition de validations (1h)

1. Tu as trois fonctions :
   - `parse(s: String): Option[Transaction]`
   - `validate(tx: Transaction): Option[Transaction]` (renvoie None si invalide)
   - `enrich(tx: Transaction): Option[Transaction]` (renvoie None si banque inconnue)
2. Utilise une `for-comprehension` pour chaîner ces trois appels.
3. Si tout est OK, tu obtiens `Some(tx)`, sinon `None`.

---

## Exercice 4 : Filtrage des erreurs (30 min)

1. Prends une liste de lignes CSV mélangées (bonnes et mauvaises).
2. Utilise `map(parseLine)` puis `flatten` pour obtenir la liste des transactions valides.
3. Compte combien de lignes ont été ignorées.

**Livrable** : Code du parser v0.4 avec gestion complète des erreurs via `Option`.
