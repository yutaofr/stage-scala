# TP Jour 3 : Mise en œuvre du Railway Pattern

**Durée :** ~4h | **Fil Rouge :** Structurer le pipeline à deux voies

---

## Exercice 1 : Visualisation (1h)

1. Prends une feuille de papier (ou un outil de dessin textuel).
2. Dessine ton pipeline actuel : `fromCsv` -> `validate` -> `forex`.
3. Pour chaque brique, indique par une flèche si elle peut envoyer la donnée vers le rail d'erreur (`Left`).

---

## Exercice 2 : Utilisation de Fold (1h30)

1. Après ton pipeline `for-yield` du TP 2, tu obtiens un `Either[ClearingError, Transaction]`.
2. Utilise `.fold` pour transformer ce résultat en une seule String de log.
3. Le succès doit afficher : `Transaction OK : MONTANT DH`.
4. L'erreur doit afficher : `REJET : RAISON`.

> ⚠️ **Attention** : `bimap` n'existe pas sur `Either` en Scala standard. Utilise `fold` qui est plus puissant car il unifie les deux types vers un seul type de retour.

---

## Exercice 3 : L'Aiguille de Récupération (1h30)

1. Ajoute une fonction `recoverNonCritical: ClearingError => Either[ClearingError, Transaction]`.
2. Si l'erreur est de type `LightWarning`, elle doit transformer l'erreur en une transaction valide (avec un flag).
3. Utilise cette fonction avec `.flatMap` ou `.orElse` sur ta chaîne d'erreurs.
4. Observe comment une donnée peut "remonter" sur le rail de succès.

**Livrable** : Schéma du pipeline et code source implémentant la gestion "Double Rail" via flatMap et bimap.
