# Semaine 19 : Industrialisation CI/CD & API (5 jours)

## Jour 1 — Docker Multi-Stage & Images Optimisées
**Cours (2h)** : Images multi-stage : builder vs runtime. Distroless pour la sécurité. Réduire la taille image de 1 Go à < 200 Mo.
**TP (4h)** : Écrire un Dockerfile multi-stage pour le moteur. Mesurer la taille avant/après.

## Jour 2 — Docker Compose Complet
**Cours (2h)** : Orchestration de l'écosystème complet. Health checks. Dépendances inter-services.
**TP (4h)** : `docker-compose.yml` qui lance Kafka KRaft, Cassandra, l'application, Prometheus et Grafana. Une commande `docker compose up --wait` démarre le laboratoire.

## Jour 3 — Intégration continue
**Cours (2h)** : GitHub Actions : compiler, tester, contrôler l'image et publier uniquement depuis une référence autorisée.
**TP (4h)** : Créer un workflow reproductible avec cache, tests et build Docker.

## Jour 4 — API REST et documentation
**Cours (2h)** : Endpoints Tapir type-safe, interprétation serveur, OpenAPI, Swagger UI et limites d'une documentation générée.
**TP (4h)** : Créer `GET /banks/{id}/position` et `GET /clearing/history?date=...`, générer OpenAPI et documenter les erreurs.

## Jour 5 — Pre-Demo
**Cours (2h)** : L'art de la démonstration technique. Storytelling : du problème à la solution.
**TP (4h)** : Répétition générale avec le tuteur. Scénario de demo end-to-end.
