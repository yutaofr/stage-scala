# TP Jour 1 : Registre des Banques & Indexation

**Durée :** ~4h | **Fil Rouge :** Améliorer la recherche et l'organisation du moteur

---

## Exercice 1 : Le Répertoire des Banques (1h)

1. Crée un objet `ReferenceData`.
2. Définis une `Map[String, String]` nommée `repertoire` qui associe un code banque (ex: "001") à son nom complet ("Attijariwafa Bank"). Ajoute au moins 5 banques marocaines.
3. Crée une fonction `getBankName(code: String): String` qui retourne le nom ou "Banque Inconnue" via `getOrElse`.

---

## Exercice 2 : Filtrage de Doublons (1h)

1. Reçois une liste d'IBANs avec des doublons.
2. Utilise un `Set` pour extraire uniquement les IBANs uniques.
3. Affiche le nombre d'IBANs uniques trouvés.

---

## Exercice 3 : Indexation par Source (2h)

1. Génère une liste de 50 transactions aléatoires (tuples `(banqueSource, banqueDest, montant)`).
2. Utilise `groupBy` pour indexer ces transactions par `banqueSource`.
3. Pour chaque banque, affiche le nombre de transactions qu'elle a envoyées.
   Exemple de sortie :
   `ATH a envoyé 12 transactions`
   `CIH a envoyé 8 transactions`

---

## Exercice 4 : Tests de Performance (Optionnel)

1. Compare le temps d'accès au premier élément d'une `List` vs le dernier élément sur une liste de 10 000 éléments.
2. Pourquoi une telle différence ?

**Livrable** : Code fonctionnel montrant l'utilisation des Maps et du groupBy pour organiser le flux de clearing.
