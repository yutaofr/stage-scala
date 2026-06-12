---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 4 — Tuning GC & JVM"
---

# Tuning GC & JVM
## Optimiser la mécanique interne de la JVM

**Durée :** ~2h | **Fil Rouge :** Réduire les pauses "Stop The World"

---

# 📋 Objectifs du Jour

- Comprendre le fonctionnement du Garbage Collector (GC).
- Découvrir les différents types de GC (Serial, Parallel, G1, ZGC).
- Apprendre à paramétrer la Heap (`-Xmx`, `-Xms`).
- Minimiser l'impact du GC sur la latence du moteur de clearing.

---

# 1. Le rôle du Garbage Collector

La JVM gère la mémoire automatiquement. Le GC identifie les objets qui ne sont plus référencés et libère leur espace.
- **Stop The World (STW)** : Moment où le GC met l'application en pause pour nettoyer. Pour un moteur de clearing, ces pauses doivent être < 10ms.

---

# 2. Les différents algorithmes

- **G1GC (Garbage First)** : Le standard actuel. Découpe la heap en régions. Très bon compromis débit/latence.
- **ZGC / Shenandoah** : Les nouveaux venus. Conçus pour des pauses ultra-faibles (< 1ms) même sur des heaps géantes.

---

# 3. Paramétrage de base

- `-Xms` : Taille initiale de la Heap.
- `-Xmx` : Taille maximale de la Heap.
- **Règle d'or** : En production, fixez `-Xms = -Xmx` pour éviter que la JVM ne passe son temps à redimensionner la mémoire.

---

# 🏗️ Application : Réglage fin pour ATH

Nous allons comparer le comportement de notre moteur sous charge avec deux configurations différentes :
1. Une configuration "par défaut".
2. Une configuration optimisée avec **G1GC** et des objectifs de temps de pause (`-XX:MaxGCPauseMillis=20`).

---

# 🧠 Quiz Rapide

1. Que signifie l'acronyme STW ? (Stop The World).
2. Pourquoi fixer -Xms et -Xmx à la même valeur ? (Pour éviter l'overhead du redimensionnement dynamique).
3. Quel flag permet d'activer G1GC ? (`-XX:+UseG1GC`).

---

# 📝 Résumé du Jour

- Le tuning GC est un art de compromis entre débit (Throughput) et latence.
- Bien régler sa Heap est la première étape d'une mise en production réussie.
- Le monitoring des pauses GC est crucial pour respecter nos SLOs de performance.

**Prochaine étape** : Trouver ton réglage optimal dans le TP 89 !
