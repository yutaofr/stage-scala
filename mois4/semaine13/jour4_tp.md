# TP Jour 4 : Mon Premier Système d'Acteurs

**Durée :** ~4h | **Fil Rouge :** Automatiser les notifications bancaires

---

## Exercice 1 : Le Coffre-Fort (Starter Kit)

> [!TIP]
> **Starter Kit Fourni :** Créer une boucle d'événements asynchrone est compliqué en 4h. Utilise le fichier `exercises/s13/ActorFramework.scala` qui te fournit toute la plomberie d'Acteurs. Ton but est d'écrire la méthode `receive` !

1. Ouvre le squelette du framework d'acteurs fourni.
2. Complète l'acteur `BankVaultActor` qui maintient la balance.
3. Implémente les messages `Debit(amount)`, `Credit(amount)` et gère les dans le pattern matching.
4. Envoie 100 messages de débit aléatoires et vérifie que le solde final est exact sans utiliser `synchronized`.

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
