# TP Jour 1 : Prise en Main de l'Environnement

**Durée :** ~4h | **Fil Rouge :** Poser les fondations du projet de compensation

---

## Exercice 1 : Vérification Docker (30 min)

1. Ouvre un terminal et lance :
   ```bash
   docker run --rm sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 sbt sbtVersion
   ```
2. Vérifie que la sortie affiche `1.7.1`.
3. Lance ensuite :
   ```bash
   docker run --rm sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 scala -version
   ```

**Livrable :** Screenshot de la sortie confirmant les versions.

---

## Exercice 2 : Premier Contact avec le REPL (45 min)

1. Ouvre le REPL Scala via Docker :
   ```bash
   docker run --rm -it sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 sbt console
   ```
2. Teste les expressions suivantes et note les résultats :
   ```scala
   val montant = 1500.50
   val banqueEmettrice = "ATH"
   val isValid = montant > 0
   
   // Essaie de modifier montant :
   montant = 2000.0   // Que se passe-t-il ?
   
   // Interpolation de chaînes
   println(s"Transaction de $montant DH émise par $banqueEmettrice")
   ```
3. **Question** : Pourquoi `val montant = 2000.0` provoque une erreur ? Quelle est la différence avec `var` ?

**Livrable :** Réponse écrite sur la différence `val` vs `var`.

---

## Exercice 3 : Import et Compilation du Starter Kit (45 min)

1. Clone le projet `magnetic-belt` dans IntelliJ.
2. Ouvre `src/main/scala/Main.scala` et lis le code.
3. Lance la compilation via Docker :
   ```bash
   docker run --rm -v $(pwd):/app -w /app \
     sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
     sbt compile
   ```
4. Lance les tests :
   ```bash
   docker run --rm -v $(pwd):/app -w /app \
     sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
     sbt test
   ```
5. Vérifie que les tests passent (Green Build ✅).

**Livrable :** Console montrant `[info] All tests passed`.

---

## Exercice 4 : Premier Code Métier (1h30)

Crée un fichier `src/main/scala/clearing/Basics.scala` :

```scala
package clearing

object Basics:
  // 1. Déclare trois constantes représentant des montants de transactions
  val transaction1: Double = ???
  val transaction2: Double = ???
  val transaction3: Double = ???

  // 2. Calcule le solde total
  val soldeTotal: Double = ???

  // 3. Crée une fonction qui affiche un résumé
  def afficherResume(): Unit =
    println(s"Nombre de transactions : 3")
    println(s"Solde total : $soldeTotal DH")
    if soldeTotal >= 0 then
      println("Position : CRÉDITRICE")
    else
      println("Position : DÉBITRICE")

  @main def run(): Unit = afficherResume()
```

**Tâches :**
1. Remplace les `???` par des valeurs réelles (positives et négatives).
2. Lance le programme via `sbt run`.
3. Modifie les montants pour obtenir un solde négatif.

**Livrable :** Code fonctionnel + sortie console dans les deux cas (créditeur et débiteur).

---

## Exercice 5 : Premier Test ScalaTest (30 min)

### 🔍 Comprendre la syntaxe des Tests

Avant d'écrire ton premier test, voici comment lire la syntaxe ScalaTest :

```scala
import org.scalatest.flatspec.AnyFlatSpec   // ← Le style "FlatSpec" (sujet + verbe)
import org.scalatest.matchers.should.Matchers // ← Les matchers (shouldBe, should contain, ...)

class BasicsSpec extends AnyFlatSpec with Matchers:
//                 ↑ Hérite du style   ↑ Mix-in : active les mots-clés d'assertion
```

| Élément | Rôle | Exemple |
|---|---|---|
| `"Le sujet"` | Décrit CE QUI est testé | `"Le solde total"` |
| `should "..."` | Décrit CE QU'ON ATTEND | `should "être positif"` |
| `in { ... }` | Contient le CODE DU TEST | `in { solde shouldBe 100 }` |
| `shouldBe` | Compare deux valeurs | `result shouldBe 42` |
| `should contain` | Vérifie la présence d'un élément | `list should contain ("ATH")` |
| `should have size` | Vérifie la taille | `list should have size 3` |

> 💡 On lit un test ScalaTest comme une **phrase en anglais** :
> *"Le solde total" should "être la somme des trois transactions"*

### 📝 À toi de jouer

Crée `src/test/scala/clearing/BasicsSpec.scala` :

```scala
package clearing

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class BasicsSpec extends AnyFlatSpec with Matchers:
  "Le solde total" should "être la somme des trois transactions" in {
    // Écris un test qui vérifie que soldeTotal == transaction1 + transaction2 + transaction3
    ???
  }
```

**Tâches :**
1. Complète le test en remplaçant `???` par une assertion `shouldBe`.
2. Lance `sbt test` et vérifie qu'il passe.

**Livrable :** Test vert.

---

## Debriefing (15 min)
- Montre tes fichiers au tuteur.
- Explique pourquoi tu as utilisé `val` et pas `var`.
- Partage ta compréhension de la compensation interbancaire en une phrase.
