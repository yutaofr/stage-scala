# TP Jour 4 : L'affineur de JVM

**Durée :** ~4h | **Fil Rouge :** Optimisation des paramètres JVM

---

## Point de départ

- Copie le **Kit 18.4** de `starter_kit_s18.md`.
- Garde la même JVM, la même image, les mêmes données et la même charge.
- Travaille dans un conteneur jetable pour les essais de faible heap.

## Exercice 1 : Diagnostic GC (1h)

1. Relance ton moteur avec le flag `-Xlog:gc*`.
2. Observe les logs de sortie : identifie la fréquence des collectes et le temps de pause moyen.
3. Utilise `jstat -gc <pid> 1000` pour voir l'évolution des générations (Eden, Survivor, Old) en temps réel.

**Validation :** relève fréquence, temps total GC, allocation et mémoire après GC.

---

## Exercice 2 : Tuning G1GC (1h30)

1. Applique les paramètres suivants à ton moteur :
   `-XX:+UseG1GC -Xms512m -Xmx512m -XX:MaxGCPauseMillis=50`
2. Relance le même test Gatling.
3. Compare débit, p95, p99 et temps GC, pas seulement la moyenne.
4. Répète trois fois.

**Validation :** le tableau indique la médiane des trois essais et leur variabilité.

---

## Exercice 3 : Stress Test & OOM (1h30)

1. Active `-XX:+HeapDumpOnOutOfMemoryError` dans le conteneur de laboratoire.
2. Réduis la heap par paliers.
3. Arrête l'expérience dès que le taux GC ou la latence franchit la limite fixée ; l'OOM n'est pas obligatoire.
4. Analyse le dernier palier soutenable.

**Validation :** la recommandation inclut une marge et les limites du test.

**Livrable** : Tableau comparatif des performances selon 3 configurations de Heap/GC différentes.
