---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 4 — Fibers & Concurrence ZIO"
---

# ZIO Fibers
## Les threads ultra-légers

**Durée :** ~2h | **Fil Rouge :** Traitement massif en parallèle

---

# 📋 Objectifs du Jour

- Comprendre la différence entre un Thread et une **Fiber**.
- Apprendre à "forker" un programme ZIO.
- Maîtriser les opérateurs de concurrence (`zipPar`, `foreachPar`).
- Gérer l'interruption des tâches asynchrones.

---

# 1. Qu'est-ce qu'une Fiber ?

Une Fiber est un "thread virtuel" géré par ZIO (pas par l'OS).
- Un thread natif réserve notamment une pile, souvent dimensionnée en centaines de kilo-octets ou davantage.
- Une Fiber stocke un état logique beaucoup plus léger, géré par le runtime.

### Pouvoir
Une application peut gérer un grand nombre de Fibers, mais la limite dépend du travail, de la mémoire et des ressources externes. Il faut mesurer et borner les accès I/O.

---

# 2. Fork & Join

```scala
for
  fiber <- longTask.fork // Lancement en arrière-plan
  _     <- otherTask     // Continue tout de suite
  res   <- fiber.join    // Attend le résultat de la fiber
yield res
```

### Opérateurs de haut niveau
Le plus souvent, on n'utilise pas `fork` directement. On utilise :
- `ZIO.foreachPar(list)(item => process(item))` : Traitement parallèle.
- `ZIO.race` : Prend le résultat du premier qui finit.

---

# 3. L'Interruption (Le Super-Pouvoir)

Si vous lancez une tâche et que vous n'en avez plus besoin, ZIO peut l'interrompre proprement.

```scala
for
  fiber <- fetchRate.fork
  _     <- ZIO.sleep(1.second)
  _     <- fiber.interrupt // "Stop tout, c'est trop long !"
yield ()
```

> [!TIP]
> Une ressource acquise dans un `Scope` est finalisée lors de l'interruption. D'autres bibliothèques JVM proposent aussi des formes de gestion structurée ; l'intérêt de ZIO est leur intégration uniforme.

---

# 🏗️ Application : Validation Parallèle v2

Nous allons valider les lignes d'un batch avec un parallélisme borné. Le gain dépendra du type de travail : CPU, réseau ou stockage.

---

# 🧠 Quiz Rapide

1. Puis-je fixer à l'avance un nombre universel de Fibers sans risque ? (Non, il faut mesurer et limiter les ressources rares).
2. Quel opérateur permet d'exécuter une liste de tâches en parallèle ? (`foreachPar`).
3. Que se passe-t-il pour une Fiber si son parent meurt ? (ZIO l'interrompt par défaut par sécurité).

---

# 📝 Résumé du Jour

- Les Fibers sont l'unité de base de la concurrence moderne.
- Elles sont extrêmement économes en ressources.
- L'interruption sûre est la clé de la résilience.
- Ton moteur de clearing est maintenant prêt pour le "Big Data".

**Prochaine étape** : Utiliser le Kit 14.4 dans le TP du Jour 4.
