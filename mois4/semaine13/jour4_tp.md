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

> [!TIP]
> **Starter Kit fourni (Kit 13.6) :** Le squelette `ClearingManager.scala` est fourni. Le protocole, l'**annuaire d'acteurs** (`Map[bankId -> ActorRef]`) et le routage des réponses sont pré-câblés. Tu n'écris que la décision *crédit ou débit ?* (ZONE STAGIAIRE).

1. Crée un acteur `ClearingManager` (cf. Kit 13.6).
2. Il reçoit le résultat du cœur v2.3 : `Map[BankCode, Money]`.
3. Pour chaque position, il retrouve l'acteur dans `Map[BankCode, ActorRef]`, puis convertit `Money` en `BigDecimal` uniquement dans le message `Credit` ou `Debit`.
4. Utilise l'opérateur `!` pour toutes les communications.

> [!NOTE]
> **Comment l'acteur destinataire est-il retrouvé ?** En Pekko Typed il n'y a pas de recherche par nom : le `ClearingManager` doit **détenir l'`ActorRef`** du coffre. L'annuaire `vaults: Map[String, ActorRef[BankVaultActor.Command]]` est cet « carnet d'adresses ». `vaults.get(bankId)` → `ActorRef` → `ref ! Credit(...)` empile le message dans la mailbox de CE coffre précis.

---

## Exercice 3 : Asynchronisme total (1h)

> [!TIP]
> **Starter Kit fourni (Kit 13.7) :** Le snippet de modification de `BankVaultActor` (ajout du `Thread.sleep` aléatoire) est fourni.

1. Ajoute un délai aléatoire (`sleep`) dans le traitement des messages du `BankVault` (Kit 13.7).
2. Observe que le `ClearingManager` continue d'envoyer ses messages sans jamais attendre, illustrant le modèle non-bloquant.

**Livrable** : Code source implémentant un mini-système d'acteurs pour la mise à jour des soldes post-clearing.
