---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 15"
footer: "Jour 4 — Garanties de Livraison & Idempotence"
---

# Garanties Kafka
## At-Least-Once vs Exactly-Once

**Durée :** ~2h | **Fil Rouge :** Éviter les doubles virements

---

# 📋 Objectifs du Jour

- Comprendre les trois niveaux de garantie : At-Most-Once, At-Least-Once, Exactly-Once.
- Apprendre à gérer les **Offsets manuellement**.
- Découvrir le concept d'**Idempotence**.
- Sécuriser le clearing contre les doublons de messages.

---

# 1. Les trois garanties

### At-Most-Once (Au plus une fois)
On commit l'offset **avant** de traiter. Si ça crash, on perd le message. ❌ Inacceptable en banque.

### At-Least-Once (Au moins une fois)
On commit l'offset **après** avoir traité. Si ça crash, on risque de traiter deux fois le même message au redémarrage. ⚠️ Risque de double débit.

### Exactly-Once (Exactement une fois)
Kafka garantit que le message n'est traité qu'une fois, même en cas de crash. ✅ Le Graal.

---

# 2. Gestion Manuelle des Offsets

Pour garantir le "At-Least-Once", on désactive l'auto-commit.

```scala
props.put("enable.auto.commit", "false")

// ... après traitement réussi ...
consumer.commitSync()
```

---

# 3. L'Idempotence : Ta Seconde Défense

Une opération est idempotente si l'exécuter plusieurs fois produit le même résultat qu'une seule fois.

### Exemple
- "Ajouter 10 DH" : **NON**-Idempotent (10 + 10 = 20).
- "Mettre le solde à 100 DH" : **Idempotent** (Toujours 100).

> [!TIP]
> Dans le clearing, nous utiliserons un `Set` des IDs de transactions déjà traitées pour rejeter les doublons (Dédoublonnage).

---

# 🏗️ Application : Le Moteur Idempotent

Nous allons ajouter un cache des `TransactionID` traités à notre moteur. Si Kafka nous renvoie une transaction déjà vue (après un crash par exemple), le moteur devra l'ignorer silencieusement.

---

# 🧠 Quiz Rapide

1. Quel réglage garantit qu'un message ne sera pas perdu, au prix d'éventuels doublons ? (At-Least-Once).
2. Pourquoi le Exactly-Once est-il gourmand en ressources ? (Car il nécessite des transactions distribuées entre Kafka et ton code).
3. Est-ce qu'un ID unique de transaction suffit pour rendre un traitement idempotent ? (Oui, s'il est utilisé pour vérifier les doublons).

---

# 📝 Résumé du Jour

- Les banques exigent des garanties de livraison parfaites.
- Le paramètre `enable.auto.commit` est le bouton de sécurité du consumer.
- L'idempotence au niveau applicatif est ta meilleure alliée.
- Ton moteur est maintenant prêt à affronter des pannes réseau sans corrompre les soldes.

**Prochaine étape** : Gérer tes offsets manuellement dans le TP 74 !
