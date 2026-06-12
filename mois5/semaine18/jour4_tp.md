# TP Jour 4 : L'affineur de JVM

**Durée :** ~4h | **Fil Rouge :** Optimisation des paramètres JVM

---

## Exercice 1 : Diagnostic GC (1h)

1. Relance ton moteur avec le flag `-Xlog:gc*`.
2. Observe les logs de sortie : identifie la fréquence des collectes et le temps de pause moyen.
3. Utilise `jstat -gc <pid> 1000` pour voir l'évolution des générations (Eden, Survivor, Old) en temps réel.

---

## Exercice 2 : Tuning G1GC (1h30)

1. Applique les paramètres suivants à ton moteur :
   `-XX:+UseG1GC -Xms512m -Xmx512m -XX:MaxGCPauseMillis=50`
2. Relance un test de charge Gatling (S18-J1).
3. Compare le débit (TPS) et la latence moyenne avec la configuration par défaut.

---

## Exercice 3 : Stress Test & OOM (1h30)

1. Réduis la Heap à une valeur trop petite (ex: `-Xmx64m`).
2. Lance une grosse charge.
3. Observe l'application "ramer" (GC Thrashing) puis tomber avec une `java.lang.OutOfMemoryError: Java heap space`.
4. Extrais les enseignements : quelle est la limite basse de mémoire pour que ton moteur tourne confortablement ?

**Livrable** : Tableau comparatif des performances selon 3 configurations de Heap/GC différentes.
