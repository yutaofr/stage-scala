# TP Jour 2 : Chaînage & For-yield sur Either

**Durée :** ~4h | **Fil Rouge :** Le pipeline "Zéro-Défaut"

---

## Exercice 1 : Chaînage Manuel (1h30)

1. Prends tes fonctions `parse` et `validate` du TP précédent.
2. Crée une fonction `processLine(line: String)` qui utilise `flatMap`.
3. Vérifie que si le parsing échoue (ex: mauvaise colonne), tu reçois bien l'erreur de parsing et non une exception de validation.

---

## Exercice 2 : Passage au For-yield (1h30)

1. Réécris `processLine` en utilisant la syntaxe `for { ... } yield ...`.
2. Ajoute une troisième étape : `applyForex: Transaction => Either[ForexError, Transaction]`.
3. Intègre cette étape dans ton `for-yield`.
4. Teste le pipeline avec une transaction qui réussit tout, et une transaction qui échoue à chaque étape différente.

---

## Exercice 3 : Collecte des Résultats (1h)

1. Traite une liste de 10 lignes CSV.
2. Utilise `map(processLine)` pour obtenir une `List[Either[ClearingError, Transaction]]`.
3. Utilise `partition` (S7) sur cette liste pour séparer les `Left` des `Right`.
4. Affiche le nombre de succès et le détail des erreurs.

**Livrable** : Code source du pipeline chaîné par for-comprehension et rapport de traitement segmenté Succès/Erreurs.
