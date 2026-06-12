# TP Jour 5 : Clearing Engine v3.0 — Le Défi du Temps Réel

**Durée :** ~4h | **Fil Rouge :** Assemblage du pipeline de streaming final

---

## Exercice 1 : Intégration Finale (2h)

1. Assemble tes deux applications :
   - `BankSimulator` : Envoie des transactions en continu.
   - `StreamingClearingEngine` : Consomme, dédoublonne, valide et affiche les résultats.
2. Vérifie que tout le pipeline fonctionne sans aucune intervention manuelle.

---

## Exercice 2 : Test de Stress Massif (1h30)

1. Configure ton simulateur pour envoyer 1000 transactions d'un coup.
2. Lance ton moteur de clearing après coup.
3. Observe la vitesse à laquelle les messages sont "ingérés" et traités.
4. Vérifie dans ton log final que le netting reste équilibré (Sum = 0) malgré la vitesse.

---

## Exercice 3 : Revue de Streaming (30 min)

1. Explique pourquoi ton architecture actuelle est plus robuste qu'un simple serveur HTTP (Indice : Kafka agit comme une file d'attente tampon si le moteur est trop lent).

**Bilan Mois 4 - Semaine 15** : Le passage au streaming est un succès total. Prêt pour la persistance finale dans Cassandra.

**Livrable** : Code source complet v3.0 (Simulator + Streaming Consumer) et démonstration du flux continu.
