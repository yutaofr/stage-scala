---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 5 — Retry & Circuit Breaker"
---

# Résilience Avancée
## Ne jamais abandonner face aux pannes réseau

**Durée :** ~2h | **Fil Rouge :** Un client HTTP indestructible

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

val resilientCall = callApi.retry(Schedule.recurs(3)) // Réessaie 3 fois
```

---

# 2. Les Schedules (Planificateurs)

ZIO permet de définir des stratégies de réessaie très précises :
- `Schedule.exponential(1.second)` : Attend de plus en plus longtemps.
- `Schedule.spaced(5.seconds)` : Attend toujours la même durée.
- `Schedule.recurs(5) && Schedule.exponential(100.millis)` : Mixez les règles !

---

# 3. Circuit Breaker : Protéger les autres

Si un service externe est totalement KO, continuer de l'appeler ne sert à rien et peut aggraver la situation (ou ralentir votre application).

### Le principe
1. **Fermé** (Normal) : Les appels passent.
2. **Ouvert** (Alerte) : Le service est KO. ZIO bloque immédiatement les appels pour ne pas perdre de temps.
3. **Demi-Ouvert** : On teste un appel pour voir si c'est réparé.

---

# 🏗️ Application : Le Gateway de Change

Nous allons équiper notre client de taux de change d'une stratégie de réessaie exponentielle. Si l'API d'ATH est instable, notre moteur patientera intelligemment au lieu d'échouer.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre `retry` et `repeat` ? (`retry` réessaie après un échec, `repeat` après un succès).
2. Pourquoi utiliser une attente exponentielle ? (Pour éviter de saturer un serveur qui est déjà en difficulté).
3. À quel moment un Circuit Breaker passe-t-il en mode "Ouvert" ? (Après un certain nombre d'échecs consécutifs).

---

# 📝 Résumé du Mois 4 (Mi-Parcours)

- **Semaine 13** : Tu as appris à gérer le matériel (Threads, Acteurs).
- **Semaine 14** : Tu as appris à gérer la logique asynchrone pure (ZIO).
- Tu es maintenant capable de construire des applications "Cloud-Native" qui ne crashent jamais.

**Prochaine étape** : Rendre ton moteur indestructible dans le TP 70 !
