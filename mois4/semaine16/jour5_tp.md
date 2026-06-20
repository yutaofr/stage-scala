# TP Jour 5 : Clearing Engine v3.1 — Déploiement & Stress-Test Final

**Durée :** ~4h | **Fil Rouge :** Soutenance technique du Mois 4

---

## Point de départ

- Utilise le Kit 16.4 avec les Kits 16.0 à 16.3.
- Pars d'un volume vide, puis répète la démonstration sans supprimer les volumes.
- Enregistre les métriques avant de conclure sur la stabilité.

## Exercice 1 : La Grande Ligne Droite (2h)

1. Relance tout ton écosystème via Docker Compose :
   - Kafka + Cassandra.
2. Lance ton simulateur à pleine vitesse.
3. Lance ton moteur de clearing v3.1.
4. Laisse tourner dix minutes.
5. Échantillonne mémoire, CPU, débit et lag toutes les minutes.
6. Conclus uniquement sur cette fenêtre d'observation ; elle ne prouve pas l'absence de fuite.

**Validation :** le tableau de mesures et les logs permettent de reproduire l'essai.

---

## Exercice 2 : Preuve de Persistance (1h30)

1. Compte les transactions par partitions banque + jour.
2. Compare avec les compteurs du simulateur et du consumer.
3. Injecte des doublons connus et vérifie le nombre d'IDs uniques.
4. Vérifie la somme des positions.

**Validation :** les égalités attendues sont écrites et accompagnées des requêtes utilisées.

---

## Exercice 3 : Revue d'Architecture (30 min)

1. Prépare un schéma montrant producer, topic/partition/offset, consumer, validation, source de vérité, projections et commit.
2. Ajoute les chemins d'échec avant et après la persistance.
3. Explique où un doublon peut apparaître et comment il est traité.

**Validation :** le schéma couvre le chemin nominal et deux chemins de reprise.

**Bilan Mois 4** : Objectifs système atteints. Le stagiaire est capable d'opérer un système distribué complexe. Prêt pour l'industrialisation finale.

**Livrable** : Code source complet v3.1, capture d'écran du monitoring et schémas d'architecture.
