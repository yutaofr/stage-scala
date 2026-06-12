---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 2 — ZLayer & Dependency Injection"
---

# ZLayer
## L'injection de dépendances réinventée

**Durée :** ~2h | **Fil Rouge :** Modulariser le ClearingService

---

# 📋 Objectifs du Jour

- Comprendre le rôle du canal **R** dans ZIO.
- Maîtriser le concept de **ZLayer** (Construction de services).
- Apprendre à assembler des dépendances complexes sans effort.
- Remplacer Spring par la puissance native de ZIO.

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

### Pourquoi c'est génial ?
- **Testabilité** : On peut échanger le "RealLayer" par un "MockLayer" en une ligne.
- **Cycle de vie** : ZIO gère l'ouverture et la fermeture des services automatiquement.

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
> Si une dépendance manque, le compilateur vous donne un message d'erreur très précis indiquant quelle pièce du puzzle est absente.

---

# 🏗️ Application : Le Moteur Modulaire

Nous allons transformer notre `ClearingService` en un service ZIO. Il dépendra de deux couches : `TransactionRepo` (I/O) et `ExchangeRateService` (HTTP).

---

# 🧠 Quiz Rapide

1. Puis-je faire tourner un programme `ZIO[Database, _, _]` avec `Any` ? (Non, erreur de compilation).
2. Quel est l'équivalent Spring du `ZLayer` ? (C'est un mélange de `@Bean` et de `@Autowired`).
3. ZIO gère-t-il les dépendances circulaires ? (Non, et c'est une bonne chose pour la conception !).

---

# 📝 Résumé du Jour

- Le canal **R** rend les dépendances de ton code explicites et vérifiables au moment de la compilation.
- `ZLayer` est l'outil ultime de modularité.
- Ton application est maintenant un assemblage de composants interchangeables et testables.
- Tu commences à voir la puissance du "Graphe de Dépendances" de ZIO.

**Prochaine étape** : Créer tes couches de clearing dans le TP 67 !
