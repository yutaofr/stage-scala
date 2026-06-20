# Revue pédagogique des semaines 14 à 20

**Objectif :** aligner les cours et les TPs sur le programme, sécuriser la progression du Clearing Engine et fournir un point de départ exploitable pour chaque exercice.

**Approche :** conserver l'organisation actuelle par semaine et par jour. Corriger les erreurs techniques dans les cours, rendre les consignes des TPs vérifiables et centraliser le code de départ dans un `starter_kit_sXX.md` par semaine.

**Socle technique :** Scala 3, ZIO 2, Apache Kafka en mode KRaft, Cassandra, OpenTelemetry, Prometheus, Grafana, Gatling, Docker Compose, GitHub Actions et Tapir.

---

## Règles éditoriales

1. Chaque notion distingue la garantie réelle de l'objectif visé.
2. Chaque TP indique ses prérequis, le kit à utiliser, les étapes, les preuves attendues et le livrable.
3. Les exercices réutilisent le projet fil rouge au lieu d'introduire un mini-projet sans lendemain.
4. Les seuils de performance et de disponibilité sont des hypothèses à mesurer, pas des garanties.
5. Les startkits fournissent l'infrastructure et laissent au stagiaire une zone de travail explicite.

## Progression du fil rouge

| Semaine | Version | Résultat attendu |
|---|---:|---|
| 14 | v2.4 | Effets ZIO, services, ressources, concurrence bornée et résilience |
| 15 | v3.0 | Ingestion Kafka avec traitement at-least-once et déduplication expliquée |
| 16 | v3.1 | Persistance Cassandra orientée requêtes et pipeline repris après panne |
| 17 | v3.2 | Logs corrélés, métriques, traces, dashboard et alertes |
| 18 | v3.3 | Limites mesurées, expériences de panne et montée en charge horizontale |
| 19 | v4.0-rc | Image, stack reproductible, CI, API documentée et répétition de livraison |
| 20 | v4.0 | Livraison vérifiée, démonstration reproductible et handover |

## Vérifications

- Liens TP → starter kit présents pour les 35 TPs.
- Numérotation des exercices unique dans chaque TP.
- Absence des anciens numéros de TP 66 à 98.
- Cohérence des noms de topics et des versions du Clearing Engine.
- Contrôle des blocs Markdown, YAML, Scala, CQL et Mermaid.
- Compilation des squelettes exécutables lorsque les dépendances du dépôt le permettent.
