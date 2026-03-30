# TP Jour 3 : Mini-Parser de Transactions

**Durée :** ~4h | **Fil Rouge :** Automatiser la lecture des flux de transactions

---

## Exercice 1 : Parser une ligne (1h30)

Crée un objet `CsvParser` dans `src/main/scala/clearing/CsvParser.scala`.

1. Implémente la méthode `parseLine(line: String): (String, String, BigDecimal)`.
2. Elle doit prendre une ligne du type `" ATH , CIH , 150.50 "` et retourner un tuple propre.
3. Utilise `split` et `trim`.

**Livrable** : Code de la méthode + test unitaire simple.

---

## Exercice 2 : Lecture multi-lignes (1h)

Implémente `parseLines(lines: List[String]): List[(String, String, BigDecimal)]`.

1. Utilise un `for-yield` pour transformer chaque ligne du fichier.
2. Filtre les lignes vides avant de parser.

---

## Exercice 3 : Résilience & Erreurs (1h)

Modifie ton parser pour qu'il soit "indestructible".

1. Si une ligne n'a pas 3 colonnes, ne crashe pas.
2. Si le montant n'est pas un nombre, ignore la ligne.
3. Affiche un message d'erreur explicite dans la console pour chaque ligne ignorée :
   `[ERROR] Ligne malformée : "ATH,500" (colonnes manquantes)`

---

## Exercice 4 : Tests de Robustesse (30 min)

Crée `CsvParserSpec.scala` et teste les cas suivants :
- Une ligne parfaite.
- Une ligne avec beaucoup d'espaces.
- Une ligne avec un montant invalide (ex: `"150,50"` au lieu de `"150.50"`).
- Une ligne vide.

**Livrable** : Suite de tests passant au vert via `sbt test`.
