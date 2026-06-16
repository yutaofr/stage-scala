---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 13"
footer: "Jour 3 — Execution Contexts & Thread Pools"
---

# Execution Contexts
## L'usine à ouvriers sous le capot

**Durée :** ~2h | **Fil Rouge :** Isoler les tâches lourdes du moteur

---

# 📋 Objectifs du Jour

- Comprendre le rôle de l'**ExecutionContext** (EC).
- Apprendre à différencier les types de pools (Fixed, Cached, ForkJoin).
- Savoir pourquoi il faut isoler les I/O bloquants du calcul CPU.
- Configurer des pools dédiés pour son application.

---

# 1. Qu'est-ce qu'un ExecutionContext ?

C'est l'abstraction Scala pour un **Thread Pool**. C'est le contremaître qui reçoit les `Future` et décide sur quel thread disponible les exécuter.

### L'import par défaut (Le pool "Global")
```scala
import scala.concurrent.ExecutionContext.Implicits.global
```
- Basé sur le nombre de cœurs de votre machine.
- Idéal pour le calcul mathématique pur.
- **Mortel** si vous faites des appels réseau bloquants à l'intérieur !

---

# 2. Le Danger des I/O bloquants

Si vous avez 8 cœurs et que vous lancez 8 appels `Thread.sleep` (ou lecture disque) sur le pool `global`, le pool entier est bloqué. Plus aucune autre Future ne peut s'exécuter.

> [!CAUTION]
> On dit que vous avez "affamé" le thread pool (**Thread Starvation**).

---

# 3. La Solution : Dédier les Pools

On utilise des pools différents selon la nature du travail.

1. **Pool de Calcul (CPU)** : Taille = Nombre de Cœurs.
2. **Pool d'I/O (Disque/Réseau)** : Grande taille, threads qui attendent.

```scala
val ioPool = ExecutionContext.fromExecutor(Executors.newCachedThreadPool())

Future { 
  readLargeFile() // Sur le pool d'I/O ✅
}(ioPool)
```

---

# 🏗️ Application : Le Moteur Multi-Pools

Nous allons segmenter notre application :
- Les validations complexes (CPU) sur le pool `global`.
- La récupération des taux (HTTP Cache/Disk) sur un `ioPool` dédié.

---

# 🧠 Quiz Rapide

1. Que se passe-t-il si j'oublie d'importer un ExecutionContext ? (Le code ne compile pas).
2. Pourquoi ne pas mettre 1000 threads sur le pool CPU ? (Parce que l'OS perdra son temps à changer de contexte plutôt qu'à calculer).
3. Est-il recommandé d'utiliser `global` pour tout ? (Non, seulement pour le calcul non-bloquant).

---

# 📝 Résumé du Jour

- L'ExecutionContext est le moteur de tes Futures.
- La segmentation des pools évite que ton application ne s'arrête à cause d'un disque lent.
- Un bon développeur Scala sait exactement où s'exécute chaque partie de son code.
- Plus tu maîtrises tes pools, plus ton application est stable sous charge.

**Prochaine étape** : Configurer tes propres pools dans le TP du Jour 3 !
