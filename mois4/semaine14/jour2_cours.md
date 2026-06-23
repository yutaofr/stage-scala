---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 2 — ZLayer & Dependency Injection"
---

# ZLayer
## L'injection de dépendances réinventée

**Durée :** ~2h | **Fil Rouge :** Brancher les services ZIO existants

---

# 📋 Objectifs du Jour

- Comprendre le rôle du canal **R** dans ZIO.
- Observer le concept de **ZLayer** pour fournir des services.
- Assembler un graphe de dépendances explicite.
- Comparer l'injection ZIO avec un conteneur d'injection classique.

---

# 1. Le canal R : Vos Outils

Quand une fonction ZIO demande un `R`, elle dit : "Je suis prête à travailler, branchez-moi sur la prise `R`."

```scala
def process: ZIO[Database, Nothing, Unit] = ...
```

Ici, `process` ne peut pas tourner tant qu'on ne lui fournit pas une implémentation de `Database`.

---

# 2. Qu'est-ce qu'un ZLayer ?

Un `ZLayer` est une usine à services. Il sait comment construire un `Database` à partir d'une `Configuration`.

```scala
val dbLayer: ZLayer[Config, Nothing, Database] = 
  ZLayer.fromFunction(config => new Database(config))
```

### Ce que ZLayer apporte
- **Testabilité** : On peut échanger le "RealLayer" par un "MockLayer" en une ligne.
- **Cycle de vie** : un `ZLayer.scoped` peut acquérir et libérer une ressource.

---

# 3. L'assemblage des couches (`provide`)

ZIO permet de "brancher" les couches comme des tuyaux.

```scala
val fullApp = myProgram.provide(
  Database.live,
  Configuration.live
)
```

> [!TIP]
> Si une dépendance manque, le type résiduel indique ce que le programme demande encore. Le message dépend toutefois de la complexité du graphe.

---

# 🏗️ Application : Le module d'observation

Nous allons utiliser les couches déjà fournies : `ValidationService.live(knownBanks)` et `NettingService.live`. Le TP ne crée pas de repository, ne crée pas de client HTTP, et ne modifie pas le cœur `clearing.core`.

---

# 🧠 Quiz Rapide

1. Puis-je faire tourner un programme `ZIO[Database, _, _]` avec `Any` ? (Non, erreur de compilation).
2. Quel est l'équivalent Spring du `ZLayer` ? (C'est un mélange de `@Bean` et de `@Autowired`).
3. ZIO gère-t-il les dépendances circulaires ? (Non, et c'est une bonne chose pour la conception !).

---

# 📝 Résumé du Jour

- Le canal **R** rend les dépendances de ton code explicites et vérifiables au moment de la compilation.
- `ZLayer` construit et partage les services nécessaires à un effet.
- Ton application est maintenant un assemblage de composants interchangeables et testables.
- Tu commences à voir la puissance du "Graphe de Dépendances" de ZIO.

**Prochaine étape** : Observer le même module unique dans le TP du Jour 2.
