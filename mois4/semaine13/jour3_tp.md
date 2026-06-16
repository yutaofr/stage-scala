# TP Jour 3 : Maîtrise des Thread Pools

**Durée :** ~4h | **Fil Rouge :** Optimiser les ressources du moteur

---

## Exercice 1 : Simulation de Starvation (1h30)

1. Configure un ExecutionContext avec seulement **2 threads**.
2. Lance 10 Futures qui font un `Thread.sleep(5000)`.
3. Lance une 11ème Future qui fait un simple `println`.
4. Observe le délai avant que le message ne s'affiche. (C'est la famine !).

---

## Exercice 2 : Création de Pools Dédiés (1h30)

1. Crée un objet `CustomPools` (voir le Kit 13.3 du starter kit).
2. Définis un `cpuPool` (FixedThreadPool avec le nombre de cœurs).
3. Définis un `ioPool` (CachedThreadPool).
4. Migre tes fonctions de validation CPU vers le premier, et tes fonctions de lecture fichier vers le second.

---

## Exercice 3 : Monitoring de Threads (1h)

1. Ajoute un `println(s"Thread: ${Thread.currentThread().getName}")` dans tes tâches.
2. Vérifie au démarrage que tes logs indiquent bien des noms de threads différents selon la nature de la tâche.

**Livrable** : Code source avec séparation explicite des ExecutionContexts pour les tâches de calcul et les tâches d'I/O.
