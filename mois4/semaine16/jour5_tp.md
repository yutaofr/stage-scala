# TP Jour 5 : Clearing Engine v3.1 — Déploiement & Stress-Test Final

**Durée :** ~4h | **Fil Rouge :** Soutenance technique du Mois 4

---

## Exercice 1 : La Grande Ligne Droite (2h)

1. Relance tout ton écosystème via Docker Compose :
   - Kafka + Cassandra.
2. Lance ton simulateur à pleine vitesse.
3. Lance ton moteur de clearing v3.1.
4. Laisse tourner 10 minutes. Vérifie qu'il n'y a aucune fuite de mémoire (CPU/RAM stable).

---

## Exercice 2 : Preuve de Persistance (1h30)

1. Après le run, effectue une requête CQL qui compte le nombre total de transactions par banque.
2. Le total doit correspondre EXACTEMENT aux messages envoyés par le simulateur (Preuve d'idempotence et de non-perte).

---

## Exercice 3 : Revue d'Architecture (30 min)

1. Prépare un schéma (peut être sur papier ou ASCII) montrant le chemin d'un virement de 100 DH depuis sa saisie dans le simulateur jusqu'à son repos final dans Cassandra.

**Bilan Mois 4** : Objectifs système atteints. Le stagiaire est capable d'opérer un système distribué complexe. Prêt pour l'industrialisation finale.

**Livrable** : Code source complet v3.1, capture d'écran du monitoring et schémas d'architecture.
