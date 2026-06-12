# TP Jour 4 : La JVM sous Stéroïdes

**Durée :** ~4h | **Fil Rouge :** Tuning et mesures d'impact

---

## Exercice 1 : Observation du GC (1h30)

1. Active les logs du GC sur ton application : `-Xlog:gc*`.
2. Lance ton moteur et observe les lignes de log.
3. Identifie les "Pause Young" et les "Pause Full". Quelle est leur durée moyenne ?

---

## Exercice 2 : Tuning G1GC (1h30)

1. Applique les réglages suivants : `-Xmx1g -Xms1g -XX:+UseG1GC`.
2. Relance ton stress test Gatling.
3. Observe si les "pics" de latence sur tes graphiques Grafana ont diminué.

---

## Exercice 3 : Passage à ZGC (1h)

1. Si ta version de Java le permet, active `-XX:+UseZGC`.
2. Compare les temps de pause avec les tests précédents.
3. Constate la disparition quasi-totale des interruptions visibles.

**Livrable** : Tableau comparatif des temps de pause GC selon les réglages utilisés.
