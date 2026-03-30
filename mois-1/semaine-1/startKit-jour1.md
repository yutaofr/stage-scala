Ouvre ton terminal et exécute ceci pas à pas.

### 1. Création de l'arborescence standard
La JVM et SBT exigent une structure stricte.

```bash
cd w
mkdir -p magnetic-belt/src/main/scala/clearing
mkdir -p magnetic-belt/src/test/scala/clearing
mkdir -p magnetic-belt/project
cd magnetic-belt
```

### 2. Verrouillage de la version SBT

Crée le fichier `project/build.properties` :
```properties
sbt.version=1.7.1
```

### 3. Le contrat de build : `build.sbt`
C'est ici que tu déclares les règles de ton projet. Pour que l'Exercice 5 (ScalaTest) fonctionne, tu dois injecter la dépendance de test.

Crée le fichier `build.sbt` à la racine de `magnetic-belt` :
```scala
name := "magnetic-belt"
version := "0.1.0-SNAPSHOT"
scalaVersion := "3.2.0"

// La librairie de test exigée par l'Exercice 5
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % Test
```

### 4. Le code factice pour valider l'Exercice 3

Crée `src/main/scala/Main.scala` :
```scala
object Main:
  @main def run(): Unit = 
    println("ATH Clearing Engine - Bootstrapping...")
```

Crée `src/test/scala/MainSpec.scala` :
```scala
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MainSpec extends AnyFlatSpec with Matchers:
  "Le moteur de compensation" should "s'initialiser correctement" in {
    val isReady = true
    isReady shouldBe true
  }
```

---

### Analyse de tes exercices (Le Miroir)

Maintenant que tu as l'environnement, regarde tes exercices avec un œil critique :
* **Exercice 2 (`val` vs `var`) :** Ce n'est pas juste une question de syntaxe. Utiliser `var` dans un moteur de compensation concurrent est un suicide architectural. Ça crée des *race conditions*. Si tu as besoin de modifier un état, tu crées une nouvelle instance. 
* **Exercice 4 (Code Métier) :** C'est du scripting basique. Ne t'attarde pas en pensant que c'est ça la programmation fonctionnelle. C'est juste un échauffement syntaxique.
* **Exercice 5 (ScalaTest) :** C'est ton véritable livrable aujourd'hui. Ton code de compensation ne vaudra rien s'il n'est pas couvert par des assertions strictes. TDD (Test-Driven Development) n'est pas une option ici, c'est une exigence de survie.

As-tu lancé les commandes Docker de l'Exercice 3 sur ce socle nouvellement créé pour me confirmer que ton terminal affiche bien `[info] All tests passed` ?
