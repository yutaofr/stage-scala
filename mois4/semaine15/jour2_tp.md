# TP Jour 2 : Injecter des données dans le sillage de Kafka

**Durée :** ~4h | **Fil Rouge :** Le simulateur de banques

---

## Point de départ

- Copie le **Kit 15.1** de `starter_kit_s15.md`.
- Utilise `sender` comme clé Kafka pour conserver l'ordre par banque émettrice.
- Ferme toujours le producer dans un `finally` ou via une ressource gérée.

## Exercice 1 : Le Producteur Simple (1h30)

1. Complète la sérialisation JSON d'une transaction déterministe.
2. Configure `acks=all` et `enable.idempotence=true`.
3. Envoie une transaction avec son ID comme en-tête et sa banque émettrice comme clé.
4. Vérifie valeur, clé, partition et offset avec le consumer console.

**Validation :** la transaction reçue se désérialise et possède le même ID que l'objet source.

---

## Exercice 2 : Générateur de Flux (1h30)

1. Génère cinquante transactions avec une seed fixe.
2. Envoie-les vers `clearing-input`.
3. Limite le rythme à dix transactions par seconde sans bloquer le thread principal si tu utilises ZIO.
4. Compte les accusés de réception réussis et échoués.

**Validation :** le nombre d'accusés de réception réussis correspond au nombre de records visibles.

---

## Exercice 3 : Asynchronisme et Callbacks (1h)

1. Utilise le callback de `producer.send`.
2. Affiche la partition et l'offset en cas de succès.
3. Arrête Kafka après quelques envois et observe les retries puis l'erreur finale.
4. Attends la fin des callbacks avant de fermer le producer.

**Validation :** le programme ne déclare pas « terminé » avant la réception de tous les callbacks.

**Livrable** : Code source du simulateur de transactions capable de peupler un topic Kafka en continu.
