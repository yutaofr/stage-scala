#!/usr/bin/env bash
set -euo pipefail

# Se placer dans le répertoire docker
cd "$(dirname "$0")/docker"

echo "=== Arrêt et nettoyage de la stack ==="
docker compose down -v --remove-orphans

echo "=== Démarrage de la stack ==="
docker compose up -d --wait

echo "=== Vérification des Healthchecks ==="
curl --fail --silent http://localhost:8080/health | grep -q "UP"

echo "=== Ingestion d'une transaction valide ==="
curl --fail --silent \
  -H 'Content-Type: application/json' \
  -d '{"id":"smoke-1","sender":"AWB","receiver":"CIH","amount":100,"status":"Pending","transactionType":"Transfer"}' \
  http://localhost:8080/api/v1/transactions

echo "=== Ingestion d'une transaction invalide (rejetée) ==="
curl -i -s -H 'Content-Type: application/json' \
  -d '{"id":"smoke-invalid","sender":"XYZ","receiver":"CIH","amount":-10,"status":"Pending","transactionType":"Transfer"}' \
  http://localhost:8080/api/v1/transactions | grep -q "400"

# TODO stagiaire :
# 1. Vérifier que la transaction valide a bien été publiée sur clearing-output
# 2. Vérifier que la transaction invalide a bien été publiée dans clearing-dlq
# 3. Vérifier via cqlsh que l'état de la transaction "smoke-1" est bien à Completed dans processing_state
# 4. Vérifier via cqlsh que les projections de positions nettes ont bien été mises à jour dans la base
???

echo "=== Smoke test réussi ! ==="
docker compose ps
