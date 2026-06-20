---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 18"
footer: "Jour 3 — Profilage JVM (VisualVM)"
---

# Profilage JVM
## Démasquer les voleurs de performance

**Durée :** ~2h | **Fil Rouge :** Pourquoi mon moteur consomme tant de RAM ?

---

# 📋 Objectifs du Jour

- Comprendre le fonctionnement de la mémoire JVM (Heap, Stack, Metaspace).
- Utiliser **VisualVM** ou **JProfiler** pour observer le code en temps réel.
- Identifier les fuites de mémoire (Memory Leaks).
- Analyser les "Hotspots" (les fonctions qui consomment le plus de CPU).

---

# 1. Anatomie de la Mémoire

- **Heap (Tas)** : Là où habitent vos objets (Transactions, Listes). Si elle est pleine -> `OutOfMemoryError`.
- **Stack (Pile)** : Là où habitent vos variables locales et vos appels de fonctions. Si trop d'appels récursifs -> `StackOverflowError`.

---

# 2. Qu'est-ce qu'un Profileur ?

C'est un outil qui "espionne" la JVM pour vous donner des graphiques :
- **CPU Profiling** : Quelle méthode prend 80% du temps ?
- **Memory Profiling** : Quel type d'objet remplit la RAM ? (ex: Des millions de Strings de logs ?).

---

# 3. La Chasse aux Memory Leaks

Une fuite arrive quand vous gardez une référence vers un objet dont vous n'avez plus besoin.
- **Exemple** : Ajouter des données dans une `Mutable Map` globale et oublier de les supprimer.

> [!TIP]
> L'immutabilité empêche certaines corruptions d'état, pas la rétention mémoire. Un cache sans limite, une Fiber non interrompue ou une queue non bornée peuvent retenir des objets.

---

# 🏗️ Application : Diagnostic en Direct

Nous allons lancer notre moteur de clearing et y connecter **VisualVM**. Nous observerons la "dent de scie" du Garbage Collector et nous tenterons de trouver quelle partie de notre code crée le plus d'objets inutiles.

---

# 🧠 Quiz Rapide

1. Que signifie l'acronyme OOM ? (OutOfMemory).
2. Quelle zone de la JVM contient les instances de vos `case class` ? (Le Heap).
3. Pourquoi un profileur ralentit-il un peu l'application ? (Parce qu'il doit collecter des données à chaque opération, ce qui ajoute un "overhead").

---

# 📝 Résumé du Jour

- Le profileur est le stéthoscope du développeur.
- Il permet de passer d'une intuition ("je pense que c'est lent là") à une certitude ("je sais que c'est cette fonction qui bloque").
- Maîtriser le profilage est indispensable pour optimiser les coûts Cloud d'une entreprise.

**Prochaine étape** : Utiliser le Kit 18.3 dans le TP du Jour 3.
