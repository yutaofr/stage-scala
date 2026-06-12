# TP Jour 3 : Le Concierge du Temps Réel

**Durée :** ~4h | **Fil Rouge :** Transformer le moteur de clearing en Consumer

---

## Exercice 1 : Le Consumer Simple (Starter Kit)

> [!TIP]
> **Starter Kit Fourni :** Gérer manuellement les offsets Kafka et la boucle ZStream prend trop de temps. Utilise le fichier `exercises/s15/KafkaConsumerBase.scala`. Concentre-toi sur l'écriture de la méthode métier `processTransaction`.

1. Ouvre le squelette `KafkaConsumerBase`.
2. Observe comment la configuration du stream et le commit sont déjà faits pour toi.
3. Complète `processTransaction(key, value)` pour qu'elle affiche simplement la valeur brute de chaque message reçu.

---

## Exercice 2 : Branchement du Domaine (1h30)

1. À l'intérieur de ta boucle, transforme chaque `String` reçue en un objet `Transaction` (utilise `Transaction.fromCsv`).
2. Appelle ton `TransactionValidator` (v2.3) pour valider la transaction.
3. Affiche : `[OFFSET: X] TX ID: Y -> STATUS: Z`.

---

## Exercice 3 : Test de Reprise (1h)

1. Lance ton simulateur (S15-J2) pour envoyer 10 messages.
2. Lance ton consumer pour les lire.
3. Arrête ton consumer (`Ctrl+C`).
4. Relance ton simulateur pour envoyer 10 nouveaux messages.
5. Relance ton consumer et vérifie qu'il lit les 10 nouveaux messages **sans relire les anciens**.

**Livrable** : Code source du consumer asynchrone intégré avec la logique de validation du moteur de clearing.
