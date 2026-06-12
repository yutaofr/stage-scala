# TP Jour 1 : Bienvenue dans le Streaming

**Durée :** ~4h | **Fil Rouge :** Setup de l'écosystème Kafka

---

## Exercice 1 : Setup Docker Compose (1h)

1. Crée un fichier `docker-compose.yml` incluant :
   - Zookeeper (Nécessaire pour les anciennes versions de Kafka).
   - Un Broker Kafka.
2. Lance l'infrastructure avec `docker-compose up -d`.
3. Vérifie que Kafka tourne en listant les topics (doit être vide).

---

## Exercice 2 : Création de Topics (1h30)

1. Utilise les outils en ligne de commande de Kafka (`kafka-topics.sh`).
2. Crée un topic nommé `clearing-transactions` avec 3 partitions et un facteur de réplication de 1.
3. Vérifie les propriétés du topic.

---

## Exercice 3 : Premier test (Produce/Consume) (1h30)

1. Utilise `kafka-console-producer.sh` pour envoyer manuellement 3 lignes CSV au format clearing.
2. Utilise `kafka-console-consumer.sh` avec l'option `--from-beginning` pour les lire en temps réel.
3. Observe que si tu tues le consumer et que tu le relances, il retrouve ses messages.

**Livrable** : Fichier `docker-compose.yml` et capture d'écran montrant l'envoi et la réception de messages via la console.
