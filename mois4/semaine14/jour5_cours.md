---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 5 — Retry borné"
---

# Retry borné
## Retenter une panne temporaire sans exagérer le module

**Durée :** ~2h | **Fil Rouge :** Publication de clearing dans le module ZIO

---

# 📋 Objectifs du Jour

- Gérer les pannes temporaires (Réseau instable).
- Utiliser la fonction `.retry` de ZIO.
- Comprendre et implémenter un **Schedule** (Planificateur).
- Distinguer une erreur temporaire d'une erreur définitive.

---

# 1. La Retentative (Retry)

Si une publication technique échoue temporairement, il est souvent utile de réessayer quelques millisecondes plus tard.

```scala
val publish: ZIO[Any, ClearingError, String] = ...

val resilientPublish = publish.retry(Schedule.recurs(3)) // Retente au plus 3 fois
```

---

# 2. Les Schedules (Planificateurs)

ZIO permet de définir des stratégies de nouvelle tentative précises :
- `Schedule.exponential(1.second)` : Attend de plus en plus longtemps.
- `Schedule.spaced(5.seconds)` : Attend toujours la même durée.
- `Schedule.recurs(5) && Schedule.exponential(100.millis)` : borne les tentatives et augmente le délai.

Ajoute du jitter pour éviter que toutes les instances ne retentent au même instant, et ne retente que les erreurs transitoires.

---

# 3. Erreur temporaire ou définitive

Un retry ne doit pas tout retenter. Dans le module, `InfrastructureFailure` simule une panne temporaire, alors que `UNKNOWN_BANK` est une erreur métier définitive.

### Le principe du TP
1. `InfrastructureFailure` : retry autorisé.
2. `UNKNOWN_BANK` : retry refusé.
3. `Schedule.recurs(3)` : borne courte et visible.

---

# 🏗️ Application : La publication de clearing

Nous allons observer `publishPositions(counter, positions).retry(infrastructureOnly)`. Le TP ne crée pas de vrai client HTTP et ne code pas de circuit breaker.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre `retry` et `repeat` ? (`retry` retente après un échec, `repeat` après un succès).
2. Pourquoi utiliser une attente exponentielle ? (Pour éviter de saturer un serveur qui est déjà en difficulté).
3. Faut-il retenter `UNKNOWN_BANK` ? (Non, ce n'est pas une panne d'infrastructure).

---

# 📝 Résumé du Mois 4 (Mi-Parcours)

- **Semaine 13** : Tu as appris à gérer le matériel (Threads, Acteurs).
- **Semaine 14** : Tu as appris à gérer la logique asynchrone pure (ZIO).
- Tu sais construire des effets composables et traiter une panne temporaire sans promettre l'absence totale d'incident.

**Prochaine étape** : Observer le même module unique dans le TP du Jour 5.
