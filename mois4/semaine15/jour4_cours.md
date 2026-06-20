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
Kafka peut rendre atomiques une production vers Kafka et l'avancement des offsets au moyen de transactions. Cette garantie ne couvre pas automatiquement un effet externe dans Cassandra ou un appel HTTP.

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
> Un `Set` mémoire permet d'apprendre le mécanisme, mais il est perdu au redémarrage. Une garantie durable exige une clé métier unique stockée avec le résultat ou un registre persistant.

---

# 🏗️ Application : Le Moteur Idempotent

Nous allons d'abord observer la déduplication avec un cache, puis préparer sa persistance pour la S16. Un doublon doit être journalisé et compté, pas ignoré silencieusement.

---

# 🧠 Quiz Rapide

1. Quel réglage garantit qu'un message ne sera pas perdu, au prix d'éventuels doublons ? (At-Least-Once).
2. Dans quel cas Kafka fournit-il une sémantique exactly-once ? (Quand les écritures Kafka et les offsets sont engagés dans la même transaction et que les consumers lisent les données validées).
3. Un ID unique suffit-il ? (Non : il faut aussi une vérification atomique et durable associée à l'effet métier).

---

# 📝 Résumé du Jour

- Les banques exigent des garanties de livraison parfaites.
- Le paramètre `enable.auto.commit` est le bouton de sécurité du consumer.
- L'idempotence au niveau applicatif est ta meilleure alliée.
- Ton moteur est maintenant prêt à affronter des pannes réseau sans corrompre les soldes.

**Prochaine étape** : Utiliser le Kit 15.3 dans le TP du Jour 4.
