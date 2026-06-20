# TP Jour 5 : Clearing Engine v3.0 — Le Défi du Temps Réel

**Durée :** ~4h | **Fil Rouge :** Assemblage du pipeline de streaming final

---

## Point de départ

- Utilise le Kit 15.4 avec les Kits 15.0 à 15.3.
- Pars d'un environnement vide avec `docker compose down -v`.
- Enregistre les commandes et les comptes de messages pour rendre la démo reproductible.

## Exercice 1 : Intégration Finale (2h)

1. Assemble tes deux applications :
   - `BankSimulator` : Envoie des transactions en continu.
   - `StreamingClearingEngine` : Consomme, dédoublonne, valide et affiche les résultats.
2. Ajoute `clearing-output` à la démonstration.
3. Vérifie la fermeture propre des deux applications.
4. Documente la politique appliquée aux messages invalides.

**Validation :** une transaction valide traverse `input → validation → output` avec le même ID.

---

## Exercice 2 : Test de Stress Massif (1h30)

1. Configure le simulateur pour envoyer 1 000 transactions avec une seed fixe.
2. Lance le consumer après la production pour mesurer le rattrapage.
3. Mesure durée, débit et nombre de records rejetés.
4. Vérifie l'invariant de netting sur les seules transactions valides.

**Validation :** `produites = valides + rejetées` et la somme des positions valides vaut zéro.

---

## Exercice 3 : Revue de Streaming (30 min)

1. Compare Kafka et un appel HTTP direct sur quatre axes : découplage, rétention, reprise et latence.
2. Liste les limites du laboratoire : un broker, réplication à `1`, déduplication mémoire.
3. Choisis les deux risques prioritaires de la S16.

**Validation :** la revue contient des preuves et ne promet pas d'exactly-once externe.

**Bilan Mois 4 - Semaine 15** : Le passage au streaming est un succès total. Prêt pour la persistance finale dans Cassandra.

**Livrable** : Code source complet v3.0 (Simulator + Streaming Consumer) et démonstration du flux continu.
