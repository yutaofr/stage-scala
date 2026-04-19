# TP Jour 1 : Migration vers les Case Classes

**Durée :** ~4h | **Fil Rouge :** Typage fort du domaine interbancaire

---

## Exercice 1 : Modélisation du Domaine (1h30)

1. Crée un fichier `src/main/scala/clearing/model/Domain.scala`.
2. Définis les structures suivantes :
   - `case class Bank(code: String, name: String)`
   - `case class Transaction(id: Int, sender: String, receiver: String, amount: BigDecimal)`
   - `case class Account(iban: String, bank: Bank, balance: BigDecimal)`
3. Ajoute une méthode `isHighValue` dans `Transaction` qui retourne true si le montant > 50 000 DH.

---

## Exercice 2 : Migration du Générateur (1h)

1. Modifie ton `TransactionGenerator` pour qu'il génère des instances de `Transaction` au lieu de tuples.
2. Vérifie que le code appelant est beaucoup plus lisible (ex: `tx.amount` au lieu de `tx._3`).

---

## Exercice 3 : Migration du Netting (1h)

1. Adapte le `NettingCalculator` pour qu'il accepte une `List[Transaction]`.
2. Utilise `foldLeft` comme précédemment, mais accède aux champs par leurs noms.
3. Vérifie que le calcul est toujours correct.

---

## Exercice 4 : Comparaison & Copie (30 min)

1. Écris un test qui crée deux instances identiques de `Bank`.
2. Vérifie via un `assert` que `bank1 == bank2`.
3. Crée une troisième banque en utilisant `.copy` à partir de `bank1` en changeant seulement le nom.

**Livrable** : Code source utilisant les case classes partout et passant les tests unitaires.
