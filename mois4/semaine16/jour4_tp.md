# TP Jour 4 : Le Dashboard Financier

**Durée :** ~4h | **Fil Rouge :** Reporting multi-dimensionnel

---

## Exercice 1 : Double Écriture (Fan-out) (1h30)

1. Crée une deuxième table Cassandra `transactions_by_amount` (Partition key: montant arrondi).
2. Modifie ton DAO pour qu'il enregistre chaque transaction dans LES DEUX tables (`transaction_history` et `transactions_by_amount`) en parallèle via `ZIO.zipPar`.
3. Vérifie que les données sont cohérentes dans les deux tables.

---

## Exercice 2 : Calcul de Solde "In-App" (1h30)

1. Implémente une méthode `calculateDailyTotal(bankId: String)` dans ton service.
2. Elle doit lire TOUTES les transactions d'une banque pour la journée depuis Cassandra et faire la somme en Scala.
3. Affiche le résultat. Observe le temps de réponse pour 1000 lignes.

---

## Exercice 3 : Simulation Dashboard (1h)

1. Code un petit programme qui rafraîchit la console toutes les 5 secondes en affichant le solde total traité depuis le début.
2. Utilise `Schedule.fixed` de ZIO pour gérer la périodicité.

**Livrable** : Code source implémentant la double écriture et un service de reporting basique sur les données Cassandra.
