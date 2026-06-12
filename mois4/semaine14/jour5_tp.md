# TP Jour 5 : Le Client HTTP Indestructible

**Durée :** ~4h | **Fil Rouge :** Finalisation de la couche asynchrone ZIO

---

## Exercice 1 : Réessaie Exponentiel (2h)

1. Simule une fonction `fetchFromUnstableAPI()` qui échoue 80% du temps.
2. Applique un `retry` avec un `Schedule.exponential(100.millis) && Schedule.recurs(5)`.
3. Affiche les horaires de chaque tentative pour vérifier la croissance de l'intervalle.
4. Teste le cas où ça finit par réussir, et le cas où ça échoue même après 5 tentatives.

---

## Exercice 2 : Le Circuit Breaker (Simulation) (1h30)

1. Utilise un `Ref` (ou une variable simple) pour compter les échecs.
2. Si les échecs dépassent 3, bloque l'accès à la fonction pendant 10 secondes (renvoie immédiatement une erreur sans appeler l'API).
3. C'est l'implémentation manuelle du concept de Circuit Breaker.

---

## Exercice 3 : Revue de Code ZIO (30 min)

1. Analyse ton moteur de clearing complet.
2. Identifie les points de friction restants avant le passage au temps réel (Kafka).

**Bilan Mois 4 - Semaine 14** : ZIO est maîtrisé. Le candidat est prêt pour le streaming de données et le Big Data.

**Livrable** : Code source v3.0 finale incluant ZIO, ZLayer, Fibers et Schedules.
