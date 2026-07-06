# Guide du Starter Kit — Semaine 15 : Apache Kafka Streaming

Bienvenue dans la semaine 15 ! Ce kit a été restructuré pour t'accompagner **pas à pas**, jour après jour, dans l'intégration d'un client Apache Kafka au sein de notre moteur de clearing.

Plutôt qu'un unique fichier volumineux et complexe, le code a été découpé en **7 modules simples** ayant chacun un rôle bien précis. Tu travailleras sur chaque module au fur et à mesure de ta progression hebdomadaire.

---

## Architecture du Starter Kit

Tous les fichiers ci-dessous se trouvent dans le répertoire `starter_kit/src/main/scala/distributed/kafka/` :

1.  [`KafkaSettings.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/KafkaSettings.scala) : Charge les configurations depuis les variables d'environnement (Bootstrap servers, Topics, Group ID). **(Fourni & Prêt)**
2.  [`RecordProcessor.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/RecordProcessor.scala) : Reçoit un message brut, le désérialise et le valide par rapport à notre domaine métier. **(Fourni & Prêt, pour te laisser te concentrer sur Kafka)**
3.  [`DeduplicationCache.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/DeduplicationCache.scala) : Registre thread-safe en mémoire pour traquer les IDs de transactions traitées. **(Fourni & Prêt)**
4.  [`TransactionProducer.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/TransactionProducer.scala) : Client producteur Kafka. **(À compléter au Jour 2)**
5.  [`BankSimulator.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/BankSimulator.scala) : Script exécutable simulant les envois des banques. **(À compléter au Jour 2)**
6.  [`ResultPublisher.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/ResultPublisher.scala) : Publie les résultats validés ou rejetés vers les topics adéquats. **(À compléter au Jour 3)**
7.  [`KafkaConsumerLoop.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/KafkaConsumerLoop.scala) : Boucle de consommation avec commit manuel et déduplication. **(À compléter aux Jours 3 et 4)**

---

## Jour 1 — Environnement Docker & Topics Kafka

L'objectif d'aujourd'hui est de démarrer l'infrastructure Kafka en local sans installer de dépendances sur ta machine, puis de créer et vérifier les topics requis.

### Étape 1 : Démarrer Kafka en mode KRaft
Rends-toi dans le répertoire du starter kit et lance le conteneur Kafka :
```bash
cd mois4/semaine15/starter_kit
docker compose -f docker/docker-compose-kafka.yml up -d --wait
```

### Étape 2 : Vérification de l'état
Vérifie que le broker est sain et répond correctement :
```bash
docker compose -f docker/docker-compose-kafka.yml ps
```

### Étape 3 : Valider les topics créés
Le script d'initialisation crée automatiquement trois topics avec **3 partitions** chacun. Inspecte-les avec la commande suivante :
```bash
docker exec -it docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```
Pour inspecter les partitions d'un topic spécifique :
```bash
docker exec -it docker-kafka-1 /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic clearing-input
```

---

## Jour 2 — Implémentation du Producteur & Simulateur

Aujourd'hui, tu vas coder la publication de messages et simuler un flux continu de transactions.

### Étape 1 : Compléter le Producteur
Ouvre [`TransactionProducer.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/TransactionProducer.scala) et complète la méthode `send` :
1. Convertis la transaction métier en contrat d'événement `TransactionSubmittedV1`.
2. Sérialise-la au format JSON via Circe (`event.asJson.noSpaces`).
3. Instancie un `ProducerRecord` en envoyant sur `settings.inputTopic`. Utilise le code de la banque émettrice (`tx.sender.value`) comme clé Kafka.
4. Ajoute l'en-tête `"transaction-id"` au record avec l'ID de la transaction sous forme de bytes UTF-8.

### Étape 2 : Compléter le Simulateur de banques
Ouvre [`BankSimulator.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/BankSimulator.scala) et complète la boucle principale :
1. Parcours la liste de transactions générées.
2. Appelle `TransactionProducer.send` de façon asynchrone pour chaque transaction.
3. Fournis un `Callback` à Kafka pour suivre le résultat de l'envoi :
   * En cas de succès : affiche un log avec la clé, le topic, la partition et l'offset retournés, puis incrémente le compteur de succès.
   * En cas d'erreur : affiche l'erreur et incrémente le compteur d'échecs.
4. Limite le débit en attendant 100ms entre chaque envoi (`Thread.sleep(100)`).

### Étape 3 : Exécution
Pour exécuter le simulateur :
```bash
sbt "runMain distributed.kafka.runBankSimulator"
```

---

## Jour 3 — Boucle de Consommation & Validation

Aujourd'hui, nous mettons en place le consumer pour écouter le topic d'entrée et valider les transactions au fil de l'eau.

### Étape 1 : Compléter la publication des résultats
Ouvre [`ResultPublisher.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/ResultPublisher.scala) et implémente la méthode `publish` :
* Si la décision est `Validated` : envoie sur `settings.outputTopic` avec l'en-tête `"transaction-id"`.
* Si la décision est `Rejected` : envoie sur `settings.dlqTopic` avec l'en-tête `"transaction-id"` (si présent).

### Étape 2 : Compléter le Consumer simple
Ouvre [`KafkaConsumerLoop.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/KafkaConsumerLoop.scala) :
1. Dans la méthode `pollOnce`, parcours les partitions du poll actuel.
2. Pour chaque record d'une partition, convertis-le en `RecordEnvelope` et appelle `processor.process(envelope)`.
3. Publie le résultat via `publisher.publish(decision)`. Pour ce premier jet, tu peux appeler `.get()` sur le future retourné pour bloquer le traitement jusqu'à ce que Kafka accuse réception de la publication (garantie At-Least-Once).
4. Commite l'offset de la partition manuellement et de façon précise en faisant `dernierOffsetTraite + 1`.

### Étape 3 : Exécution
Lance le consumer pour traiter le flux :
```bash
sbt "runMain distributed.kafka.runKafkaConsumer"
```
*Astuce : Utilise `Ctrl+C` pour l'arrêter et vérifie dans les logs que le Shutdown Hook intercepte le signal pour fermer proprement les ressources.*

---

## Jour 4 — Garanties de Livraison & Idempotence

Aujourd'hui, tu vas ajouter la gestion de la déduplication et tester la robustesse de ton application face aux pannes.

### Étape 1 : Intégrer le cache de déduplication
Modifie la méthode `pollOnce` de [`KafkaConsumerLoop.scala`](file:///Users/weizhang/w/stage-scala/mois4/semaine15/starter_kit/src/main/scala/distributed/kafka/KafkaConsumerLoop.scala) :
1. Avant de traiter un message, vérifie si l'ID de la transaction est présent dans `DeduplicationCache`.
2. S'il est présent, passe directement au message suivant (c'est un doublon !).
3. Une fois que la publication sur le topic de sortie ou DLQ a été validée avec succès, marque l'ID de transaction comme traité en appelant `DeduplicationCache.markProcessed(id)`.

### Étape 2 : Simuler une panne et vérifier la relecture (TP Jour 4 Ex 1 & 2)
1. Ajoute temporairement un crash provoqué dans ton code (ex: `System.exit(1)`) juste après l'envoi de la décision mais **avant** l'appel à `commitSync`.
2. Lance le consumer, puis le simulateur.
3. Observe le crash. Relance le consumer : constate qu'au démarrage, Kafka te donne à nouveau les mêmes offsets non validés. C'est le comportement attendu en mode **At-Least-Once**.
4. Constate que grâce au `DeduplicationCache`, les doublons techniques sont détectés.

---

## Jour 5 — Intégration Finale & Tests de Robustesse

Aujourd'hui, nous assemblons tout le pipeline et mesurons la performance et l'exactitude des calculs.

### Étape 1 : Lancer la validation complète
1. Vide les topics pour repartir de zéro :
   ```bash
   docker compose -f docker/docker-compose-kafka.yml down -v
   docker compose -f docker/docker-compose-kafka.yml up -d --wait
   ```
2. Lance le consumer en arrière-plan :
   ```bash
   sbt "runMain distributed.kafka.runKafkaConsumer" &
   ```
3. Exécute le simulateur pour injecter les 50 transactions :
   ```bash
   sbt "runMain distributed.kafka.runBankSimulator"
   ```

### Étape 2 : Vérification de l'invariant
Vérifie le nombre total de messages dans chaque topic pour s'assurer que :
$$\text{messages dans input} = \text{messages dans output} + \text{messages dans DLQ}$$

Tu peux inspecter le nombre de messages à l'aide des outils en ligne de commande intégrés au conteneur Kafka.

---

## Exécuter les tests du Starter Kit avec Docker

Pour s'assurer de la bonne compilation et du passage de tous les tests sans polluer ta machine hôte, utilise l'environnement Docker de test :
```bash
docker compose -f docker/docker-compose-test.yml up --build
```
Ceci va compiler les sources et exécuter `sbt test` de manière totalement isolée.
