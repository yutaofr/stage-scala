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
- Implémenter la boucle de consommation (poll loop).
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
consumer.subscribe(Collections.singletonList("clearing-input"))
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
> La boucle doit tolérer les périodes sans record, mais elle doit aussi pouvoir s'arrêter proprement et fermer le consumer.

---

# 3. Intégration ZIO

Une version ultérieure pourra utiliser **zio-kafka**. Le TP conserve d'abord le client Java pour rendre visibles le poll, le traitement et le commit.

```scala
Consumer
  .plainStream(Subscription.topics("clearing-input"), Serde.string, Serde.string)
  .mapZIO(record => process(record.value))
  .runDrain
```

> 💡 Une bibliothèque facilite le câblage, mais l'application doit encore choisir quand un offset peut être validé et comment traiter un record invalide.

---

# 🏗️ Application : Le Moteur Connecté

Nous allons coder le "pont" entre Kafka et notre logique métier. Le moteur va lire chaque message, le valider, et afficher le résultat en temps réel au lieu de le faire par batch à la fin de la journée.

---

# 🧠 Quiz Rapide

1. Puis-je avoir deux consumers du même groupe sur la même partition ? (Non, un seul à la fois).
2. Que se passe-t-il si je relance mon consumer avec le même `group.id` ? (Il reprend à l'offset validé, selon la politique de reset si aucun offset n'existe).
3. À quoi sert le timeout de `poll()` ? (À borner l'attente et à rendre la boucle réactive aux événements de contrôle).

---

# 📝 Résumé du Jour

- Le Consumer est le cœur réactif de ton application.
- Les `group.id` permettent de répartir la charge de travail.
- Ton moteur de clearing n'attend plus les fichiers : il vit au rythme des transactions du pays.

**Prochaine étape** : Utiliser le Kit 15.2 dans le TP du Jour 3.
