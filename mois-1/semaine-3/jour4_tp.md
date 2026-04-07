# TP Jour 4 : Performance & Gros Volumes

**Durée :** ~4h | **Fil Rouge :** Optimiser le moteur pour 100 000 transactions

---

## Exercice 1 : Stress Test Simple (1h)

1. Génère une liste de 100 000 transactions aléatoires.
2. Mesure le temps de calcul du Netting (avec ton code du Jour 3).
3. Utilise `System.currentTimeMillis()` pour mesurer le temps d'exécution.

---

## Exercice 2 : List vs Vector (1h)

1. Effectue le même calcul avec une `List` puis avec un `Vector`.
2. Mesure la différence de temps pour :
   - Le parcours complet (sum).
   - L'accès répété au 50 000ème élément.
3. Note tes observations dans un commentaire.

---

## Exercice 3 : Optimisation avec View (1h30)

1. Crée un pipeline complexe : `filter -> map -> map -> filter -> sortBy`.
2. Lance-le sur les 100 000 transactions.
3. Ajoute `.view` avant la première étape et mesure la différence de performance (et théoriquement de consommation mémoire).

---

## Exercice 4 : Prévenir le StackOverflow (30 min)

1. Essaie de sommer 1 000 000 d'entiers avec une fonction récursive NON-terminale. Observe l'erreur `StackOverflowError`.
2. Réécris-la avec `foldLeft` ou une récursion terminale et vérifie que ça passe.

**Livrable** : Rapport de performance comparatif et code optimisé utilisant `Vector` et `view` si pertinent.
