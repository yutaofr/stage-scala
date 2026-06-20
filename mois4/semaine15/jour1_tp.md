# TP Jour 1 : Bienvenue dans le Streaming

**Durée :** ~4h | **Fil Rouge :** Setup de l'écosystème Kafka

---

## Point de départ

- Copie le **Kit 15.0** de `starter_kit_s15.md`.
- Utilise `clearing-input`, `clearing-output` et `clearing-dlq` pendant toute la semaine.
- Vérifie l'état du broker avant de créer les topics.

## Exercice 1 : Setup Docker Compose (1h)

1. Copie le Compose KRaft fourni ; n'ajoute pas ZooKeeper.
2. Lance `docker compose up -d kafka`.
3. Attends que le healthcheck soit vert avec `docker compose ps`.
4. Liste les topics depuis le conteneur Kafka.

**Validation :** le broker est `healthy` et la commande CLI répond sans erreur de connexion.

---

## Exercice 2 : Création de Topics (1h30)

1. Lance le service `kafka-init` fourni.
2. Vérifie que les trois topics ont chacun trois partitions.
3. Explique pourquoi le facteur de réplication reste à `1` dans ce laboratoire à un seul broker.
4. Note qu'une réplication à `1` n'offre aucune tolérance à la panne du broker.

**Validation :** `kafka-topics.sh --describe` affiche trois partitions pour chacun des trois topics.

---

## Exercice 3 : Premier test (Produce/Consume) (1h30)

1. Lance un console consumer sur `clearing-input` avec un `group.id`.
2. Envoie trois transactions JSON avec le console producer.
3. Arrête puis relance le consumer avec le même groupe : il reprend après l'offset validé.
4. Relance avec un autre groupe et `--from-beginning` : il relit l'historique disponible.

**Validation :** le compte rendu distingue clairement rétention des records et position de lecture du groupe.

**Livrable** : Fichier `docker-compose.yml` et capture d'écran montrant l'envoi et la réception de messages via la console.
