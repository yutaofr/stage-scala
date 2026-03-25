# TP Jour 5 : Mini-Prototype de Clearing (Projet de Fin de Semaine)

**Durée :** ~4h | **Fil Rouge :** Assembler TOUT le code de la semaine en un premier moteur fonctionnel

---

## Projet : Le Mini-Clearing Engine v0.1

Tu vas créer un programme principal qui enchaîne les étapes suivantes :

### Architecture Cible
```
Génération → Validation → Filtrage → Calcul Netting → Rapport
```

---

## Étape 1 : Le Programme Principal (1h)

Crée `src/main/scala/clearing/ClearingEngine.scala` :

```scala
package clearing

object ClearingEngine:
  @main def runClearing(): Unit =
    println("=== MOTEUR DE COMPENSATION v0.1 ===\n")

    // 1. Générer 50 transactions aléatoires
    val rawTransactions = TransactionGenerator.generateBatch(50)
    println(s"Transactions générées : ${rawTransactions.size}")

    // 2. Valider chaque transaction
    val (valid, invalid) = rawTransactions.partition { case (_, _, amount) =>
      Validator.isPositiveAmount(amount)
    }
    println(s"Valides : ${valid.size} | Rejetées : ${invalid.size}\n")

    // 3. Calculer le solde net par banque
    val byBank: Map[String, List[(String, String, BigDecimal)]] = ???
    // Grouper les transactions valides par banque (1er élément du tuple)

    // 4. Afficher le rapport pour chaque banque
    for (bank, txs) <- byBank do
      val amounts = txs.map(_._3)
      println(NettingCalculator.summary(bank, amounts))

    // 5. Afficher le total compensé
    val totalNet = ??? // Somme de tous les soldes nets (doit être ≈ 0)
    println(s"\n=== Solde net global : $totalNet DH ===")
```

**Tâche :** Remplacer les `???` par du vrai code.

---

## Étape 2 : Enrichir le Rapport (1h)

Ajoute au rapport de chaque banque :
- Le montant de la plus grosse transaction.
- Le montant moyen.
- La catégorie dominante (utilise `TransactionCategorizer`).

---

## Étape 3 : Tests d'Intégration (1h)

Crée `src/test/scala/clearing/ClearingEngineSpec.scala` :

```scala
package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ClearingEngineSpec extends AnyFlatSpec with Matchers:
  "Le moteur" should "traiter un batch sans crash" in {
    val batch = TransactionGenerator.generateBatch(100)
    batch should not be empty
  }

  "Le netting global" should "être proche de zéro" in {
    // La somme de toutes les positions nettes de toutes les banques
    // devrait théoriquement être 0 (ce qui entre = ce qui sort)
    // Teste cette propriété !
    ???
  }
```

---

## Étape 4 : Démonstration & Debriefing (1h)

1. Lance le programme complet : `docker run --rm -v $(pwd):/app -w /app ... sbt run`
2. Prépare un mini résumé (5 lignes) de ce que tu as appris cette semaine.
3. Identifie **3 problèmes** dans le code actuel :
   - Que se passe-t-il s'il y a 0 transactions pour une banque ?
   - Le code est-il vraiment typé ? (On utilise des `String` et des `Tuple` partout...)
   - Peut-on facilement ajouter un nouveau type de transaction ?

**Ces 3 problèmes seront résolus dans les semaines suivantes grâce aux ADTs, à Option et au Pattern Matching !**

---

## Bilan Semaine 1

| Acquis | Fichier |
|---|---|
| Types & BigDecimal | `Basics.scala`, `CurrencyConverter.scala` |
| Contrôle & Matching | `TransactionCategorizer.scala` |
| Fonctions & Récursion | `Validator.scala`, `NettingCalculator.scala` |
| Intégration | `ClearingEngine.scala` |

**Tu as construit un prototype v0.1 du moteur de compensation.** La semaine prochaine, on l'améliore avec les `for-comprehensions` et un `pattern matching` plus avancé.
