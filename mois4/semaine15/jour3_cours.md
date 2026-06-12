---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 15"
footer: "Jour 3 — Consumers & Streaming"
---

# Kafka Consumers
## Recevoir et traiter les données au fil de l'eau

**Durée :** ~2h | **Fil Rouge :** Le moteur de clearing temps réel

---

# 📋 Objectifs du Jour

- Utiliser la bibliothèque `KafkaConsumer` en Scala.
- Implémentez la boucle de consommation (Poll Loop).
- Comprendre la gestion automatique et manuelle des Offsets.
- Intégrer notre moteur v2.3 dans le flux de consommation.

---

# 1. Le Consumer Group

Contrairement au Producer, un Consumer s'inscrit dans un **Group**.
Kafka mémorise l'avancement de chaque groupe de manière indépendante.

```scala
val props = new Properties()
props.put("group.id", "clearing-engine-group")
// configuration...

val consumer = new KafkaConsumer[String, String](props)
consumer.subscribe(Collections.singletonList("clearing-transactions"))
```

---

# 2. La Boucle Infinie (Poll)

Le mode de fonctionnement de Kafka est le "Pull" (On va chercher les données).

```scala
while (true) {
  val records = consumer.poll(Duration.ofMillis(100))
  for (record <- records) {
    val tx = Transaction.fromCsv(record.value())
    process(tx) // C'est ici que ton moteur v2.3 intervient !
  }
}
```

> [!CAUTION]
> Une boucle de consommation ne doit jamais s'arrêter, sauf lors de l'arrêt gracieux (Shutdown) de l'application.

---

# 3. Intégration ZIO

Dans un monde réel, on utiliserait **ZIO-Kafka** pour transformer cette boucle bloquante en un flux asynchrone élégant.

```scala
Consumer.subscribeAnd(Subscription.topics("clearing-transactions"))
  .flatMap(_.map(record => process(record.value)).runDrain)
```

> 💡 ZIO-Kafka se charge des threads, de la gestion des erreurs et des offsets pour nous.

---

# 🏗️ Application : Le Moteur Connecté

Nous allons coder le "pont" entre Kafka et notre logique métier. Le moteur va lire chaque message, le valider, et afficher le résultat en temps réel au lieu de le faire par batch à la fin de la journée.

---

# 🧠 Quiz Rapide

1. Puis-je avoir deux consumers du même groupe sur la même partition ? (Non, un seul à la fois).
2. Que se passe-t-il si je relance mon consumer avec le même `group.id` ? (Il reprend à l'offset où il s'était arrêté).
3. Pourquoi utiliser un délai dans le `poll()` ? (Pour ne pas faire chauffer le CPU à vide si le topic est désert).

---

# 📝 Résumé du Jour

- Le Consumer est le cœur réactif de ton application.
- Les `group.id` permettent de répartir la charge de travail.
- Ton moteur de clearing n'attend plus les fichiers : il vit au rythme des transactions du pays.

**Prochaine étape** : Brancher ton moteur sur Kafka dans le TP 73 !
