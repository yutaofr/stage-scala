---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 3 — Pipeline Complet (Kafka → ZIO → Cassandra)"
---

# L'Architecture de Données
## Du flux temps réel au stockage durable

**Durée :** ~2h | **Fil Rouge :** Assemblage du pipeline final v3.1

---

# 📋 Objectifs du Jour

- Réunir toutes les briques du Mois 4.
- Créer un flux ZIO de bout en bout (End-to-End).
- Apprendre à orchestrer le passage des données entre Kafka et Cassandra.
- Gérer les erreurs sur toute la chaîne.

---

# 1. Le Pipeline de Données ATH

Le cycle de vie complet d'une transaction est maintenant :
1. **Source** : Un Producer Kafka (Banque).
2. **Buffer** : Un Topic Kafka (Transport).
3. **Moteur** : Un Consumer ZIO (Validation & Clearing).
4. **Archive** : Une table Cassandra (Persistance).

---

# 2. Orchestration ZIO

Grâce à ZIO, l'assemblage est un simple bloc `for`.

```scala
for
  record <- KafkaConsumer.poll
  tx     <- validate(Transaction.fromCsv(record))
  _      <- TransactionRepo.save(tx)
  _      <- KafkaConsumer.commit
yield ()
```

- Si le `save` échoue, le `commit` n'est pas fait (Sécurité).
- Tout est asynchrone et non-bloquant.

---

# 3. La Latence vs Le Débit

Dans un système distribué, il faut trouver l'équilibre :
- **Latence** : Temps pour qu'UNE transaction soit traitée (doit être < 100ms).
- **Débit** : Nombre de transactions par seconde (doit être > 10 000/s).

> [!TIP]
> ZIO excelle dans les deux grâce à ses Fibers ultra-légères.

---

# 🏗️ Application : Clearing Engine v3.1

Nous allons assembler les TP de la S15 (Kafka) et de la S16 (Cassandra). Le résultat sera une application autonome qui consomme en direct et archive en direct. C'est l'état de l'art du développement backend.

---

# 🧠 Quiz Rapide

1. Si Cassandra est lent, que se passe-t-il dans Kafka ? (Les messages s'accumulent dans le topic sans être perdus).
2. Pourquoi est-il important de commiter l'offset APRÈS l'écriture dans Cassandra ? (Pour garantir que la donnée est bien sauvée avant de dire à Kafka qu'on a fini).
3. Mon CPU est à 100% mais mon débit est faible, que faire ? (Vérifier si le goulot d'étranglement est le réseau ou la base de données).

---

# 📝 Résumé du Jour

- Tu as construit un pipeline de données complet.
- ZIO sert de "colle" intelligente entre les systèmes.
- Ton application est maintenant une pièce maîtresse du système d'information.
- Tu maîtrises le cycle de vie complet de l'information financière.

**Prochaine étape** : Assembler ton pipeline complet dans le TP 78 !
