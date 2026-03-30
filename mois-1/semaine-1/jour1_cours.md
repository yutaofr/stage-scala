---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 1"
footer: "Jour 1 — Bienvenue & Environnement Scala"
---

# Bienvenue & Environnement de Développement
## Stage Scala & Programmation Fonctionnelle

**Durée :** ~2h | **Fil Rouge :** Compensation Interbancaire

---

# 📋 Objectifs du Jour

- Comprendre le contexte du stage (ATH & Compensation).
- Découvrir la philosophie de Scala vs Java.
- Comprendre le rôle de la JVM.
- Configurer l'environnement de développement avec Docker & SBT.

---

# 1. Présentation du Stage

### Le contexte ATH
- Acteur clé de la **compensation interbancaire** au Maroc.
- Traitement de millions de transactions quotidiennes.
- **Ton défi** : Construire un moteur de compensation distribué d'ici 5 mois.

---

# 🏦 Qu'est-ce que la Compensation ?

Exemple : Tu paies 50 DH à un ami dans une autre banque.
1. La banque A débite 50 DH.
2. La banque B crédite 50 DH.
3. À la fin de la journée : Calcul du **solde net** interbancaire (**Netting**).
4. Seul le solde net est transféré physiquement.

---

# 2. Pourquoi Scala ?

### Origine & Vision
- Créé par **Martin Odersky** (auteur de `javac`).
- **SCA**lable **LA**nguage.

### Les 3 Piliers ATH
1. **Typage statique fort** : Moins de bugs en production.
2. **Programmation Fonctionnelle** : Code prévisible et composable.
3. **Écosystème JVM** : Puissance de Java + Elégance de Scala.

---

# ⚔️ Scala vs Java

```java
// Java : Plus verbeux
public class Calculator {
    public static double add(double a, double b) {
        return a + b;
    }
}
```

```scala
// Scala : Expressif et concis
def add(a: Double, b: Double): Double = a + b
```

---

# 3. La JVM en 10 minutes

### Le Voyage du Code
1. Code source `.scala`
2. Compilateur `scalac` → **Bytecode** `.class`
3. **JVM** exécute partout (Cloud, Mac, Linux).

### Avantages
- **Performance** (JIT Compilation).
- **Gestion mémoire** (Garbage Collector).
- **Interopérabilité** totale avec Java.

---

# 4. L'Environnement Docker

### Pourquoi Docker chez ATH ?
- **Zéro installation** sur la machine hôte.
- **Isolément** : Pas de conflit de versions.
- **Règle d'or** : Builds et tests *obligatoirement* via Docker.

---

# 🐳 Notre Image de Base

```dockerfile
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0
```

- **JDK 17** : Fondations modernes.
- **SBT 1.7.1** : Le moteur de build.
- **Scala 3.2.0** : La version cible du stage.

---

# 🛠️ Commandes Essentielles

```bash
# Compiler le projet
docker run --rm -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt compile

# Lancer les tests
docker run --rm -v $(pwd):/app -w /app \
  sbtscala/scala-sbt:eclipse-temurin-17.0.4_1.7.1_3.2.0 \
  sbt test
```

---

# 5. SBT — Scala Build Tool

### Structure du Projet Standard
```text
magnetic-belt/
├── build.sbt            ← Configuration
├── src/
│   ├── main/scala/      ← Code métier
│   └── test/scala/      ← Tests unitaires
```

---

# ⌨️ Aide-mémoire SBT

| Commande | Action |
|---|---|
| `sbt compile` | Compile tout le projet |
| `sbt test` | Lance la suite de tests |
| `sbt run` | Exécute l'application |
| `sbt console` | Lance le REPL (Console interactive) |

---

# 🧠 Quiz Rapide

1. Que signifie le nom "Scala" ?
2. Pourquoi préférons-nous Docker pour le build ?
3. Quelle est la différence majeure entre Scala et Java vue aujourd'hui ?

---

# 📝 Résumé du Jour

- La compensation réduit le nombre de transferts réels.
- Scala combine l'orienté objet et le fonctionnel sur la JVM.
- SBT et Docker sont tes outils quotidiens.

**Prochaine étape** : Ton premier code Scala dans le TP 1 !
