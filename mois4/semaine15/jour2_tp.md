# TP Jour 2 : Injecter des données dans le sillage de Kafka

**Durée :** ~4h | **Fil Rouge :** Le simulateur de banques

---

## Exercice 1 : Le Producteur Simple (1h30)

1. Crée un objet `ClearingProducer`.
2. Configure-le pour se connecter à ton broker local.
3. Envoie une seule transaction codée en dur.
4. Utilise la console Kafka (`kafka-console-consumer`) pour vérifier que le message est bien arrivé.

---

## Exercice 2 : Générateur de Flux (1h30)

1. Crée une boucle qui génère 50 transactions aléatoires.
2. Utilise ton `Transaction.toCsv` pour formater le message.
3. Envoie-les toutes dans le topic `clearing-transactions`.
4. Ajoute un petit délai (`Thread.sleep(100)`) pour simuler une activité continue.

---

## Exercice 3 : Asynchronisme et Callbacks (1h)

1. Modifie ton envoi pour utiliser la version avec Callback : `producer.send(record, (metadata, exception) => ...)`.
2. Affiche la partition et l'offset où chaque transaction a été enregistrée.
3. Force une erreur (en éteignant Docker Kafka) et observe comment le producteur réagit via l'exception dans le callback.

**Livrable** : Code source du simulateur de transactions capable de peupler un topic Kafka en continu.
