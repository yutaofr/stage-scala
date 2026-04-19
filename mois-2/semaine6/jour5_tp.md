# TP Jour 5 : Clearing Engine v1.1 — Le Moteur Communicant

**Durée :** ~4h | **Fil Rouge :** Rapport d'erreurs et robustesse totale

---

## Exercice 1 : Assemblage Global (2h)

1. Rassemble tes modules pour créer `ClearingAppV11`.
2. Le moteur doit lire un fichier avec des données de test variées.
3. **Sortie attendue** :
   ```text
   [SUCCESS] 45 transactions traitées avec succès.
   [WARNING] 3 transactions suspectes (Fraud Signal).
   [ERROR] 12 lignes ignorées :
     - Ligne 4 : IBAN invalide
     - Ligne 15 : Montant négatif
     - Ligne 22 : Banque inconnue
   [REPORT] Position nette finale par banque...
   ```

---

## Exercice 2 : Test de Cas Limites (1h30)

1. Crée un fichier de test contenant 0 transactions.
2. Crée un fichier ne contenant QUE des erreurs.
3. Vérifie que ton application affiche un rapport cohérent et ne crashe jamais.

---

## Exercice 3 : Revue Technique (30 min)

1. Montre à ton tuteur comment tu as utilisé les `sealed traits` pour le reporting.
2. Explique l'intérêt d'avoir rendu les constructeurs de `Iban` ou `Transaction` privés.

**Bilan Mois 2 - Semaine 6** : Ton moteur est maintenant "de grade production". Il est capable de dialoguer avec l'utilisateur et de se protéger des mauvaises données.

**Livrable** : Code source v1.1 complet et suite de tests de robustesse.
