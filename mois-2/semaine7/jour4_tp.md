# TP Jour 4 : Benchmark & Big Data

**Durée :** ~4h | **Fil Rouge :** Atteindre les performances industrielles

---

## Exercice 1 : Stress-Test Industriel (1h30)

1. Génère une liste de **1 000 000** de transactions aléatoires.
2. Mesure le temps nécessaire pour calculer le netting multilatéral.
3. Compare le temps entre une implémentation utilisant `List` et une utilisant `Vector`.
4. Note le temps en millisecondes.

---

## Exercice 2 : Optimisation Pipeline (1h)

1. Crée un pipeline avec 4 transformations (`filter` -> `map` -> `filter` -> `map`).
2. Mesure le temps d'exécution sur 1 million de lignes.
3. Ajoute `.view` au début et mesure à nouveau.
4. Analyse la différence.

---

## Exercice 3 : Parallélisation (1h)

1. Ajoute une opération volontairement lente dans ta validation (ex: `Thread.sleep(1)` ou un calcul de hash complexe).
2. Mesure le temps de traitement séquentiel sur 1000 transactions.
3. Utilise `.par` pour paralléliser le traitement.
4. Observe le gain de performance selon le nombre de cœurs de ta machine (simulateur).

---

## Exercice 4 : Rapport de Scalabilité (30 min)

1. Rédige un court rapport indiquant :
   - Le point de rupture de ton moteur (combien de millions de transactions avant le crash mémoire).
   - Tes recommandations de structures de données pour la production.

**Livrable** : Code optimisé et rapport de benchmark.
