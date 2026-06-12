# TP Jour 2 : Le Pouvoir des Chiffres

**Durée :** ~4h | **Fil Rouge :** Instrumentation Prometheus du moteur

---

## Exercice 1 : Installation du Connecteur (1h)

1. Ajoute les dépendances `zio-metrics-connectors`.
2. Configure ton application pour exposer un serveur HTTP sur le port `8080`.
3. Vérifie que l'URL `http://localhost:8080/metrics` renvoie du texte au format Prometheus.

---

## Exercice 2 : Compteurs & Histogrammes (1h30)

1. Ajoute un counter `clearing_processed_total` avec un tag `status` (success/failure).
2. Ajoute un histogramme `clearing_processing_duration_seconds` pour mesurer le temps passé dans la logique de netting.
3. Fais tourner ton simulateur et rafraîchis la page `/metrics` pour voir les chiffres évoluer.

---

## Exercice 3 : Lancer Prometheus (1h30)

1. Ajoute un service `prometheus` à ton `docker-compose.yml`.
2. Crée un fichier `prometheus.yml` pour lui dire de "scraper" ton application Scala.
3. Connecte-toi à l'interface de Prometheus (`http://localhost:9090`) et trace ton premier graphique de débit de transactions.

**Livrable** : Code source instrumenté et capture d'écran du graphe Prometheus montrant l'activité du moteur.
