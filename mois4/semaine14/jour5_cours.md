---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 5 — Retry & Circuit Breaker"
---

# Résilience Avancée
## Réagir sans aggraver une panne réseau

**Durée :** ~2h | **Fil Rouge :** Un client HTTP résilient

---

# 📋 Objectifs du Jour

- Gérer les pannes temporaires (Réseau instable).
- Utiliser la fonction `.retry` de ZIO.
- Comprendre et implémenter un **Schedule** (Planificateur).
- Découvrir le concept de **Circuit Breaker** pour protéger son système.

---

# 1. La Retentative (Retry)

Si un appel API échoue (ex: timeout passager), il est souvent utile de réessayer quelques secondes plus tard.

```scala
val callApi: ZIO[Any, String, Data] = ...

val resilientCall = callApi.retry(Schedule.recurs(3)) // Retente au plus 3 fois
```

---

# 2. Les Schedules (Planificateurs)

ZIO permet de définir des stratégies de nouvelle tentative précises :
- `Schedule.exponential(1.second)` : Attend de plus en plus longtemps.
- `Schedule.spaced(5.seconds)` : Attend toujours la même durée.
- `Schedule.recurs(5) && Schedule.exponential(100.millis)` : borne les tentatives et augmente le délai.

Ajoute du jitter pour éviter que toutes les instances ne retentent au même instant, et ne retente que les erreurs transitoires.

---

# 3. Circuit Breaker : Protéger les autres

Si un service externe est totalement KO, continuer de l'appeler ne sert à rien et peut aggraver la situation (ou ralentir votre application).

### Le principe
1. **Fermé** (Normal) : Les appels passent.
2. **Ouvert** (Alerte) : Le service est KO. ZIO bloque immédiatement les appels pour ne pas perdre de temps.
3. **Demi-Ouvert** : On teste un appel pour voir si c'est réparé.

---

# 🏗️ Application : Le Gateway de Change

Nous allons équiper notre client de taux de change d'une stratégie de nouvelle tentative avec backoff exponentiel. Si l'API est temporairement instable, le moteur espace ses appels avant de retourner une erreur.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre `retry` et `repeat` ? (`retry` retente après un échec, `repeat` après un succès).
2. Pourquoi utiliser une attente exponentielle ? (Pour éviter de saturer un serveur qui est déjà en difficulté).
3. À quel moment un Circuit Breaker passe-t-il en mode "Ouvert" ? (Après un certain nombre d'échecs consécutifs).

---

# 📝 Résumé du Mois 4 (Mi-Parcours)

- **Semaine 13** : Tu as appris à gérer le matériel (Threads, Acteurs).
- **Semaine 14** : Tu as appris à gérer la logique asynchrone pure (ZIO).
- Tu sais construire des effets composables et traiter plusieurs classes de panne sans promettre l'absence totale d'incident.

**Prochaine étape** : Utiliser le Kit 14.5 dans le TP du Jour 5.
