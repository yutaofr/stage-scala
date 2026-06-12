# TP Jour 3 : Sherlock Holmes de la JVM

**Durée :** ~4h | **Fil Rouge :** Analyse de performance avec VisualVM

---

## Exercice 1 : Connexion JMX (1h)

1. Lance ton moteur de clearing avec les flags JMX activés (pour permettre la connexion externe).
2. Ouvre `VisualVM` (ou un outil équivalent).
3. Connecte-toi au processus de ton application.
4. Observe le graphique de la Heap.

---

## Exercice 2 : Détection de "Hotspots" (1h30)

1. Lance une simulation de charge avec Gatling (S18-J1).
2. Dans VisualVM, va dans l'onglet "Sampler" ou "Profiler" > "CPU".
3. Identifie la fonction qui consomme le plus de CPU. Est-ce le parsing CSV ? La validation ? L'écriture Cassandra ?

---

## Exercice 3 : Capture de Heap Dump (1h30)

1. Provoque volontairement une accumulation d'objets (ex: ne pas vider un cache).
2. Effectue un "Heap Dump" (capture de la mémoire).
3. Analyse le dump pour trouver quelle classe occupe le plus d'octets.
4. Corrige le code pour libérer les références.

**Livrable** : Rapport d'analyse CPU montrant les fonctions les plus gourmandes et capture d'écran de l'analyse de mémoire.
