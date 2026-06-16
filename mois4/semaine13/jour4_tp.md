# TP Jour 4 : Mon Premier Système d'Acteurs

**Durée :** ~4h | **Fil Rouge :** Automatiser les notifications bancaires

---

## Exercice 1 : Le Coffre-Fort (Starter Kit)

> [!TIP]
> **Starter Kit Fourni (Kit 13.4) :** Le squelette de l'acteur Pekko Typed est fourni dans `distributed/actors/BankVaultActor.scala`. Les messages (`Credit`, `Debit`, `GetBalance`) et les réponses sont déjà définis. Ton but est d'écrire la **logique métier** dans le pattern matching !

1. Ouvre `distributed/actors/BankVaultActor.scala` (Kit 13.4 du starter kit).
2. Complète la logique du `Credit` : calculer le nouveau solde et retourner `TransactionResult(true, newBalance)`.
3. Complète la logique du `Debit` : vérifier que le solde est suffisant avant de débiter. Si le solde est insuffisant, retourner `TransactionResult(false, balance)` sans modifier l'état.
4. Lance un `ActorSystem` avec 5 acteurs (un par banque) et envoie-leur 100 messages concurrents. Vérifie que les soldes finaux sont exacts sans utiliser `synchronized`.

---

## Exercice 2 : Le Guichet Central (Orchestrateur) (1h30)

1. Crée un acteur `ClearingManager`.
2. Il doit recevoir une liste de résultats de netting.
3. Pour chaque résultat, il doit envoyer le bon message de crédit/débit aux acteurs `BankVault` concernés.
4. Utilise l'opérateur `!` pour toutes les communications.

---

## Exercice 3 : Asynchronisme total (1h)

1. Ajoute un délai aléatoire (`sleep`) dans le traitement des messages du `BankVault`.
2. Observe que le `ClearingManager` continue d'envoyer ses messages sans jamais attendre, illustrant le modèle non-bloquant.

**Livrable** : Code source implémentant un mini-système d'acteurs pour la mise à jour des soldes post-clearing.
