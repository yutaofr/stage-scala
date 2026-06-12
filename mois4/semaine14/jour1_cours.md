---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 1 — Qu'est-ce qu'un Effet ? ZIO[R, E, A]"
---

# Introduction à ZIO
## Dompter les effets de bord avec élégance

**Durée :** ~2h | **Fil Rouge :** Un mini-moteur de clearing ZIO

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Programmation Orientée Effets**.
- Découvrir la structure de type `ZIO[R, E, A]`.
- En finir avec le monde impur (I/O, Exceptions).
- Écrire son premier programme "Bonjour le Monde" avec ZIO.

---

# 1. Qu'est-ce qu'un Effet ?

Un effet est une description de ce que le programme doit faire.
- `Future` : Lance le travail immédiatement.
- `ZIO` : Est une **recette**. Tant qu'on ne l'exécute pas, rien ne se passe.

### Pourquoi c'est mieux ?
C'est 100% pur (Referential Transparency). On peut manipuler les actions comme des données complexes.

---

# 2. Le Type ZIO[R, E, A]

C'est "l'équation de la vie" en Scala moderne :
- **R** (Environment) : De quoi le programme a besoin pour tourner ? (ex: Database).
- **E** (Error) : Quel type d'erreur peut-il renvoyer ? (ex: ClearingError).
- **A** (Value) : Quel résultat il produit en cas de succès ? (ex: Transaction).

```scala
val program: ZIO[Any, IOException, Unit] = Console.printLine("Hello ATH")
```

---

# 3. La Sécurité Totale

Avec ZIO, les erreurs ne sont plus des interruptions (exceptions). Ce sont des valeurs typées dans le canal **E**.

### Comparaison
- JVM brute : `def read(): String` (Peut crasher si le fichier manque).
- ZIO : `def read(): ZIO[Any, FileNotFoundError, String]` (Le compilateur t'oblige à gérer l'absence de fichier).

---

# 🏗️ Application : Premier Hello ZIO

Nous allons créer un programme ZIO qui demande le nom d'une banque à l'utilisateur et l'affiche. C'est simple, mais c'est la base de tout ce qui suit.

---

# 🧠 Quiz Rapide

1. Une `Future` est-elle paresseuse (Lazy) ? (Non, elle s'exécute de suite).
2. Que représente le `R` dans `ZIO[R, E, A]` ? (Les dépendances / ressources).
3. Si mon programme ne peut pas échouer, quel type mettre pour `E` ? (`Nothing`).

---

# 📝 Résumé du Jour

- ZIO transforme les actions impures en blueprints purs.
- Le typage `[R, E, A]` documente parfaitement le comportement de ton code.
- ZIO est le standard actuel pour les applications Scala à haute performance.
- Bienvenue dans le monde du "Zéro Effet de Bord Subi".

**Prochaine étape** : Tes premiers programmes ZIO dans le TP 66 !
