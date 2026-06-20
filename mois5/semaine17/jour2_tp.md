# TP Jour 2 : Le Pouvoir des Chiffres

**Durée :** ~4h | **Fil Rouge :** Instrumentation Prometheus du moteur

---

## Point de départ

- Copie le **Kit 17.2** de `starter_kit_s17.md`.
- Garde une nomenclature unique : `clearing_transactions_processed_total`.
- Limite les labels à des ensembles bornés comme `status`.

## Exercice 1 : Installation du Connecteur (1h)

1. Ajoute les dépendances `zio-metrics-connectors`.
2. Configure ton application pour exposer un serveur HTTP sur le port `8080`.
3. Vérifie que l'URL `http://localhost:8080/metrics` renvoie du texte au format Prometheus.

**Validation :** `curl -f` réussit et la page contient au moins une métrique du moteur.

---

## Exercice 2 : Compteurs & Histogrammes (1h30)

1. Ajoute un counter `clearing_transactions_processed_total` avec un tag `status`.
2. Ajoute un histogramme `clearing_processing_duration_seconds` pour mesurer le temps passé dans la logique de netting.
3. Choisis des buckets cohérents avec les durées observées.
4. Fais tourner le simulateur et vérifie les séries.

**Validation :** le total des statuts correspond aux transactions terminées et aucun label n'a une cardinalité non bornée.

---

## Exercice 3 : Lancer Prometheus (1h30)

1. Lance le service Prometheus du kit.
2. Vérifie la cible dans `/targets`.
3. Trace `sum(rate(clearing_transactions_processed_total[5m]))`.
4. Arrête le moteur et observe la métrique `up`.

**Validation :** la cible passe de `UP` à `DOWN` et le graphe de débit utilise une unité par seconde.

**Livrable** : Code source instrumenté et capture d'écran du graphe Prometheus montrant l'activité du moteur.
