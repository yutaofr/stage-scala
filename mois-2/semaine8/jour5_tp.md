# TP Jour 5 : Clearing Engine v1.3 — Mise en Production Simulée

**Durée :** ~4h | **Fil Rouge :** Assemblage final du moteur connecté

---

## Exercice 1 : Intégration Finale (2h)

1. Ton application doit maintenant :
   - Charger des transactions depuis un fichier CSV.
   - Hasher les IBANs par sécurité.
   - Récupérer le taux de change MAD via le service HTTP.
   - Calculer le netting multilatéral.
   - Exposer le tout via un `ClearingService` typé Spring.

---

## Exercice 2 : Robustesse & "Fault Tolerance" (1h30)

1. Simule une panne réseau (le service de change renvoie `None`).
2. Vérifie que ton moteur :
   - Ne s'arrête pas brusquement.
   - Log l'erreur proprement.
   - Utilise un taux par défaut ou rejette uniquement les transactions concernées.

> [!NOTE]
> **Piste d'amélioration (Mois 3) :** Actuellement, les transactions rejetées (erreurs de parsing, banques non autorisées) sont simplement "ignorées" du calcul final. En production, elles devraient être routées vers un flux de **rejets** (fichier CSV dédié, table BDD) pour qu'un opérateur métier puisse les analyser, corriger et re-soumettre. On implémentera ce pattern au Mois 3 avec `Either[ClearingError, Transaction]`.

---

## Exercice 3 : Démo de Fin de Mois (30 min)

1. Présente le pipeline complet à ton tuteur.
2. Explique comment le passage aux ADTs a facilité l'ajout des nouvelles fonctionnalités de la semaine 8.

**Bilan Mois 2** : Énorme progression. Le stagiaire est prêt pour les concepts théoriques profonds du mois 3.

**Livrable** : Code source complet v1.3, documenté, avec simulation HTTP et Spring.
