# TP Jour 1 : Migration vers Either

**Durée :** ~4h | **Fil Rouge :** Un moteur de clearing qui explique ses échecs

---

> 💡 **Lien avec ta rétro S9** : Tu trouvais lourd de trimballer `(List[Transaction], List[ClearingError])` à chaque étape du pipeline. Aujourd'hui, on va faire en sorte que chaque transaction "porte" son statut (succès ou erreur) individuellement grâce à `Either`.

## Exercice 1 : Parsing Typé (1h30)

1. Reprends ta méthode `Transaction.fromCsv`.
2. Modifie-la pour qu'elle retourne `Either[ParsingError, Transaction]`.
3. `ParsingError` doit être un nouveau type de ton ADT `ClearingError`.
4. Gère les différents cas d'échec (Nombre de colonnes incorrect, Montant non numérique) avec des messages spécifiques dans le `Left`.

---

## Exercice 2 : Validation avec Raison (1h30)

1. Modifie ton `Validator.check`.
2. Au lieu de retourner un `Boolean` ou une `Option`, retourne `Either[ValidationError, Transaction]`.
3. Si la transaction est valide, renvoie-la dans un `Right`. sinon renvoie l'erreur détaillée dans un `Left`.

---

## Exercice 3 : Consommation exhaustive (1h)

1. Écris une boucle (ou un `map`) qui traite une liste de lignes CSV.
2. Pour chaque ligne, utilise le pattern matching pour afficher soit le succès, soit le détail de l'erreur rencontrée.
3. Vérifie que tu n'as plus aucun `get` ou `isDefined` dans ton code.

**Livrable** : Code source utilisant `Either` pour le parsing et la validation, avec reporting détaillé des erreurs.
