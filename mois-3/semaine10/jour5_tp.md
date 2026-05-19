# TP Jour 5 : Clearing Engine v2.1 — Zéro Exception

**Durée :** ~4h | **Fil Rouge :** Intégration de la résilience complète

---

## Exercice 1 : Pipeline Intégral Typé (2h)

1. Finalise ton application `ClearingAppV21`.
2. Le pipeline doit être construit à 100% avec des `Either` (en convertissant les `Try` si nécessaire).
3. Utilise la syntaxe `for-yield` pour le bloc principal de traitement d'une transaction.
4. Le netting final ne doit être calculé QUE sur les transactions qui ont survécu à tout le rail de succès.

---

## Exercice 2 : Statistiques d'Erreurs (1h30)

1. Après le passage du batch, produis un rapport qui compte les erreurs par type :
   - Combien de `ParsingError` ?
   - Combien de `ValidationError` ?
   - Combien de `TechnicalError` (Issues de `Try`) ?
2. Utilise le pattern matching sur le `Left` pour faire ce décompte.

---

## Exercice 3 : Revue de Résilience (30 min)

1. Montre à ton tuteur que tu peux injecter des données totalement corrompues dans le moteur sans qu'il ne lève jamais une `Exception` fatale.

**Bilan Mois 3 - Semaine 10** : Le moteur est désormais "Bullet-proof". La gestion d'erreurs monadique est acquise.

**Livrable** : Code source v2.1 exhaustif, rapport de statistiques d'erreurs et démonstration de non-crash.
