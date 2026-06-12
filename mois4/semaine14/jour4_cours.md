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
- 1 Thread JVM = ~1 Mo de mémoire.
- 1 Fiber ZIO = ~Humains octets.

### Pouvoir
Vous pouvez lancer **plusieurs centaines de milliers** de Fibers sur une seule machine sans la ralentir.

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
> ZIO garantit que même en cas d'interruption, les ressources (`Scope`) sont libérées. C'est unique sur la JVM.

---

# 🏗️ Application : Validation Parallèle v2

Nous allons réécrire notre moteur de clearing pour valider toutes les lignes d'un batch en parallèle. Grâce à `foreachPar`, nous allons diviser le temps de traitement par le nombre de cœurs de la machine.

---

# 🧠 Quiz Rapide

1. Puis-je lancer 1 million de Fibers ? (Oui).
2. Quel opérateur permet d'exécuter une liste de tâches en parallèle ? (`foreachPar`).
3. Que se passe-t-il pour une Fiber si son parent meurt ? (ZIO l'interrompt par défaut par sécurité).

---

# 📝 Résumé du Jour

- Les Fibers sont l'unité de base de la concurrence moderne.
- Elles sont extrêmement économes en ressources.
- L'interruption sûre est la clé de la résilience.
- Ton moteur de clearing est maintenant prêt pour le "Big Data".

**Prochaine étape** : Paralléliser tes traitements avec ZIO dans le TP 69 !
