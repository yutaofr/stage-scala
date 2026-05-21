# TP Jour 3 : Pipelines & LEGOs Fonctionnels

**Durée :** ~4h | **Fil Rouge :** Assembler le pipeline de clearing v2.0

---

## Exercice 1 : Micro-fonctions de Nettoyage (1h30)

1. Crée un objet `DataCleaner`.
2. Définis trois fonctions pures :
   - `cleanIban: String => String` (supprime les espaces et met en majuscules).
   - `formatAmount: BigDecimal => BigDecimal` (arrondit à 2 décimales).
   - `validateStatus: Transaction => Either[Error, Transaction]` (met le statut à `Rejected` si le montant est <= 0).
3. Teste chaque fonction de manière isolée.

---

## Exercice 2 : La Grande Chaîne (1h30)

1. Utilise `andThen` pour créer une fonction `fullPipeline` qui enchaîne :
   - Le parsing CSV (depuis S8).
   - Le nettoyage (depuis l'exercice 1).
   - L'anonymisation (depuis S8-J1).
2. Applique cette fonction sur une liste de chaînes CSV brutes.

---

## Exercice 3 : Inversion et Composition (1h)

1. Expérimente avec `compose` pour inverser l'ordre de certaines opérations de formatage.
2. Observe que le résultat change selon l'ordre (`andThen` vs `compose`).
3. Ajoute une étape de log (pure) qui affiche le passage entre chaque brique (Indice : utilise une fonction `id` qui affiche et retourne la valeur).

**Livrable** : Code source montrant l'assemblage du pipeline par composition de fonctions indépendantes.
