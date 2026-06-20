---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 15"
footer: "Jour 2 — Producers Scala"
---

# Kafka Producers
## Envoyer des données dans le flux

**Durée :** ~2h | **Fil Rouge :** Simulateur de trafic bancaire

---

# 📋 Objectifs du Jour

- Utiliser la bibliothèque `KafkaProducer` en Scala.
- Configurer les paramètres clés (Acks, Retries, Idempotence).
- Envoyer des objets de domaine (Transactions) sérialisés en String.
- Comprendre le rôle des Callbacks et des Futures Java.

---

# 1. Le Producteur Indispensable

Pour que le clearing commence, il faut injecter des données. Le Producer est le point de départ du pipeline.

```scala
val props = new Properties()
props.put("bootstrap.servers", "localhost:9092")
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

val producer = new KafkaProducer[String, String](props)
```

---

# 2. Envoyer un message

```scala
val record = new ProducerRecord[String, String]("topic-name", "key", "value-csv")
producer.send(record)
```

### Garanties de livraison (`acks`)
- `acks=0` : Le producteur n'attend aucune réponse (Super rapide, peu fiable).
- `acks=1` : Le leader a bien reçu le message.
- `acks=all` : toutes les répliques actuellement dans l'ISR ont accusé réception. Avec une réplication à `1`, cela reste un seul broker.

Pour réduire les doublons produits lors des retries, active `enable.idempotence=true`. Cette option ne rend pas tout le traitement métier exactly-once.

---

# 3. Sérialisation des Objets

Nous allons utiliser notre Type Class `ClearingSerializable` (S11) pour transformer nos `Transaction` en `String` juste avant l'envoi.

> [!IMPORTANT]
> Kafka ne traite que des octets (`Array[Byte]`). On utilise souvent une couche de String intermédiaire (CSV/JSON) pour débuter.

---

# 🏗️ Application : Le Simulateur de Flux

Nous allons coder un programme qui génère 100 transactions aléatoires par seconde et les injecte dans Kafka. C'est ce qui simulera l'activité des banques au Maroc.

---

# 🧠 Quiz Rapide

1. Quelle classe Kafka permet de représenter un message prêt à être envoyé ? (`ProducerRecord`).
2. Pourquoi faut-il fermer (`close()`) le producteur à la fin ? (Pour vider les buffers et ne pas perdre les derniers messages en mémoire).
3. Un producteur peut-il envoyer vers plusieurs topics ? (Oui).

---

# 📝 Résumé du Jour

- Le Producer est l'entrée de ton système de streaming.
- La configuration des `acks` définit ton niveau de confiance.
- La sérialisation transforme tes objets métier en octets pour le réseau.
- Ton système commence à avoir un "débit".

**Prochaine étape** : Utiliser le Kit 15.1 dans le TP du Jour 2.
