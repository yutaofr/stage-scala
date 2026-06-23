---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 1 — Qu'est-ce qu'un Effet ? ZIO[R, E, A]"
---

# Introduction à ZIO
## Dompter les effets de bord avec élégance

**Durée :** ~2h | **Fil Rouge :** Un module ZIO d'observation dans le projet du stagiaire

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Programmation Orientée Effets**.
- Découvrir la structure de type `ZIO[R, E, A]`.
- Isoler les I/O et rendre leurs erreurs explicites.
- Lire un petit module ZIO qui enveloppe le cœur Scala existant.

---

# 1. Qu'est-ce qu'un Effet ?

Un effet est une valeur qui décrit ce que le programme devra faire. L'effet reste pur tant que le runtime ne l'exécute pas.
- `Future` : Lance le travail immédiatement.
- `ZIO` : Est une **recette**. Tant qu'on ne l'exécute pas, rien ne se passe.

### Pourquoi c'est utile ?
On peut composer, tester, répéter ou interrompre la description avant son exécution. Les effets de bord existent toujours, mais ils sont localisés et pilotés.

---

# 2. Le Type ZIO[R, E, A]

C'est "l'équation de la vie" en Scala moderne :
- **R** (Environment) : De quoi le programme a besoin pour tourner ? (ex: Database).
- **E** (Error) : Quel type d'erreur peut-il renvoyer ? (ex: ton ADT d'erreur métier).
- **A** (Value) : Quel résultat il produit en cas de succès ? (ex: ta transaction ou tes positions nettes).

```scala
val program: ZIO[Any, IOException, Unit] = Console.printLine("Hello ATH")
```

---

# 3. Erreurs typées, défauts et interruption

Les erreurs prévues apparaissent dans le canal **E**. ZIO distingue aussi les défauts non récupérables et l'interruption d'une Fiber : le type `E` ne remplace donc pas toute la gestion des incidents.

### Comparaison
- JVM brute : `def read(): String` peut lever une exception non visible dans la signature.
- ZIO : `def read(): ZIO[Any, FileReadError, String]` rend l'échec attendu visible et permet un traitement exhaustif.

---

# 🏗️ Application : Premier module ZIO personnel

Chaque stagiaire branche ZIO sur les fonctions qu'il a déjà écrites : validation `Either`, netting pur, et petit batch de référence. Le calcul métier reste le même; ZIO rend visibles l'exécution différée, l'erreur typée et les dépendances.

---

# 🧠 Quiz Rapide

1. Une `Future` est-elle paresseuse (Lazy) ? (Non, elle s'exécute de suite).
2. Que représente le `R` dans `ZIO[R, E, A]` ? (Les dépendances / ressources).
3. Si mon programme ne peut pas échouer, quel type mettre pour `E` ? (`Nothing`).

---

# 📝 Résumé du Jour

- ZIO transforme les opérations avec effets en descriptions composables.
- Le typage `[R, E, A]` documente les dépendances, les erreurs attendues et le résultat.
- Le runtime exécute l'effet et gère les Fibers, l'interruption et les ressources.

**Prochaine étape** : Adapter la trame du starter kit dans le TP du Jour 1.
