# Starter Kit Semaine 20 : Livraison et passation

## Kit 20.1 — Smoke test final

```bash
#!/usr/bin/env bash
set -euo pipefail

docker compose down -v --remove-orphans
docker compose config >/dev/null
docker compose up -d --wait

curl --fail --silent http://localhost:8080/health
curl --fail --silent http://localhost:8080/docs >/dev/null

curl --fail --silent \
  -H 'Content-Type: application/json' \
  -d '{"id":"smoke-1","sender":"AWB","receiver":"CIH","amount":100,"status":"Pending","transactionType":"Transfer"}' \
  http://localhost:8080/api/v1/transactions

# TODO stagiaire : vérifier output, DLQ, état Completed et projections.
docker compose ps
```

## Kit 20.2 — Plan de deck

| # | Idée | Preuve | Durée | Transition |
|---:|---|---|---:|---|
| 1 | Besoin métier | exemple de flux | 1 min | du problème à l'architecture |
| 2 | Architecture | diagramme v4.0 | 2 min | de l'architecture aux garanties |
| 3 | Garanties | offsets + IDs | 2 min | des garanties aux mesures |
| 4 | Observabilité | dashboard + trace | 2 min | des mesures à la démo |
| 5 | Limites et suite | backlog priorisé | 1 min | conclusion |

## Kit 20.3 — Script de démonstration

```text
Étape :
Commande :
Résultat attendu :
Preuve affichée :
Durée maximale :
Condition de passage au secours :
Plan de secours :
```

Jeu de données minimal :

- une transaction valide ;
- une transaction invalide ;
- un doublon ;
- un batch équilibré ;
- un ID connu pour la trace.

## Kit 20.4 — Grille de soutenance

```text
Temps respecté :
Besoin métier compris :
Architecture expliquée :
Garantie at-least-once expliquée :
Limites de l'exactly-once expliquées :
Démo nominale :
Démo résilience :
Mesures citées avec leur protocole :
Question sans réponse immédiate :
Action de suivi :
Responsable :
Échéance :
```

## Kit 20.5 — Handover

```markdown
# Handover Clearing Engine v4.0

## Périmètre et responsabilités
## Architecture et flux de données
## Démarrage, arrêt et smoke test
## Configuration et secrets
## Topics Kafka et consumer groups
## Tables Cassandra et requêtes servies
## Dashboards, alertes et runbooks
## CI, image et procédure de release
## Garanties et limites connues
## Incidents fréquents et diagnostic
## Décisions d'architecture
## Backlog priorisé
## Contacts et propriétaires
```
