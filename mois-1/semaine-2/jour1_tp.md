# TP S2-J1 : Enrichir le Moteur avec les Tuples et For-Comprehensions

**Durée :** ~4h | **Fil Rouge :** Améliorer le moteur v0.1 avec des données structurées

---

## Exercice 1 : Transactions Structurées (1h)

Remplace les tuples basiques par des tuples nommés plus riches :

```scala
package clearing

object TransactionV2:
  // Un tuple de 5 éléments : (ID, Émetteur, Récepteur, Montant, Type)
  type Transaction = (Int, String, String, BigDecimal, String)

  def generateBatch(n: Int): List[Transaction] =
    (1 to n).toList.map { id =>
      val sender = TransactionGenerator.banks(scala.util.Random.nextInt(5))
      val receiver = ???  // une banque différente du sender
      val amount = TransactionGenerator.generateAmount()
      val txType = TransactionGenerator.types(scala.util.Random.nextInt(4))
      (id, sender, receiver, amount, txType)
    }
```

---

## Exercice 2 : Rapports avec For-Comprehension (1h30)

```scala
object ReportGenerator:
  def bilateralReport(transactions: List[TransactionV2.Transaction]): Unit =
    // Pour chaque paire (banqueA, banqueB), calculer :
    // - Le nombre de transactions de A vers B
    // - Le montant total de A vers B
    // Utiliser une for-comprehension avec deux générateurs
    ???

  def topTransactions(transactions: List[TransactionV2.Transaction], n: Int): List[TransactionV2.Transaction] =
    // Retourner les n plus grosses transactions (par montant absolu)
    ???
```

---

## Exercice 3 : Matrice de Compensation (1h)

Génère un tableau de compensation bilatérale :

```
         ATH      CIH      BOA
ATH      -      +2500    -1200
CIH    -2500      -      +800
BOA    +1200    -800       -
```

---

## Exercice 4 : Tests (30 min)

Teste que :
- Le batch généré a exactement `n` éléments.
- Les `topTransactions` sont bien triées par montant décroissant.
- La matrice bilatérale est symétrique (A→B = -(B→A)).
