# Semaine 19 : Industrialisation CI/CD & API (5 jours)

## Jour 1 — Docker Multi-Stage & Images Optimisées
**Cours (2h)** : Images multi-stage : builder vs runtime. Distroless pour la sécurité. Réduire la taille image de 1 Go à < 200 Mo.
**TP (4h)** : Écrire un Dockerfile multi-stage pour le moteur. Mesurer la taille avant/après.

## Jour 2 — Docker Compose Complet
**Cours (2h)** : Orchestration de l'écosystème complet. Health checks. Dépendances inter-services.
**TP (4h)** : `docker-compose.yml` qui lance : Kafka + Zookeeper + Cassandra + App + Prometheus + Grafana. Un seul `docker-compose up` pour tout démarrer.

## Jour 3 — API REST avec Tapir
**Cours (2h)** : Exposer les résultats de clearing via une API HTTP. Tapir : endpoints type-safe avec doc OpenAPI auto-générée.
**TP (4h)** : Créer les endpoints : `GET /banks/{id}/position`, `GET /clearing/history?date=...`. Générer le Swagger.

## Jour 4 — Documentation Technique
**Cours (2h)** : Swagger UI. README.md complet. Architecture Decision Records (ADR).
**TP (4h)** : Rédiger la doc technique : architecture, guide de déploiement, configuration. Publier le Swagger.

## Jour 5 — Pre-Demo
**Cours (2h)** : L'art de la démonstration technique. Storytelling : du problème à la solution.
**TP (4h)** : Répétition générale avec le tuteur. Scénario de demo end-to-end.
