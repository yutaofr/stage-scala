---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 16"
footer: "Jour 5 — Demo Mois 4 (Clearing Engine v3.1)"
---

# Bilan du Mois 4
## Du Code au Système Distribué

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v3.1 — La Plateforme Bancaire de Demain

---

# 📋 Objectifs du Jour

- Récapituler l'aventure du Mois 4 (Concurrence, ZIO, Kafka, Cassandra).
- Comprendre pourquoi "l'Orchestration" est le cœur du métier de Lead Dev.
- Visualiser la montée en compétence : d'un script `main` à un écosystème Dockerisé.
- Découvrir le programme du Mois 5 : Industrialisation (CI/CD, Kubernetes).

---

# 1. Rétrospective du Mois 4

### Semaine 13 & 14 : Le Moteur Asynchrone
- Maîtrise des Threads et des Acteurs.
- Passage à ZIO pour une gestion parfaite des effets et des ressources.

### Semaine 15 & 16 : L'Infrastructure de Données
- Kafka pour le transport asynchrone et le dédoublonnage.
- Cassandra pour les lectures prévisibles et les projections par banque.

---

# 🛡️ État des Lieux de la Solution (v3.1)

Votre application est désormais une véritable **Plateforme** :
- **Temps Réel** : Traitement instantané via Kafka.
- **Résilience** : Auto-réparatrice via les Acteurs et les Retries ZIO.
- **Persistance** : archivage répliquable dans Cassandra, avec garanties dépendantes du cluster et du niveau de cohérence.
- **Modularité** : Entièrement configurable via ZLayer.

---

# 🚀 Vers le Mois 5 : Industrialisation

On quitte le monde du développement pour celui de la **Production** :
1. **Tests d'Intégration** : Utiliser `TestContainers` pour automatiser les tests avec Kafka et Cassandra.
2. **CI/CD** : Builder l'image Docker avec `GitHub Actions` ou `SBT Native Packager`.
3. **Observabilité** : Monitorer le moteur avec `Prometheus` et `Grafana`.
4. **Performance et haute disponibilité** : mesurer les limites et lancer plusieurs instances.
5. **Projet final** : préparer une livraison et une présentation vérifiables.

---

# 🧠 Grand Quiz du Mois 4

1. Pourquoi ZIO est-il préférable aux Futures Java pour un projet de longue durée ?
2. Quel est l'avantage majeur de Kafka sur une file d'attente classique (RabbitMQ) ?
3. Pourquoi avoir choisi Cassandra plutôt que MySQL pour l'archivage ?
4. Comment as-tu géré la back-pressure (si la base est plus lente que le flux) ?

---

# 📝 Conclusion

Tu as franchi un cap : tu raisonnes désormais en composants, dépendances et garanties. Le mois final vérifiera l'exploitabilité du prototype et préparera une livraison démontrable.

**Dernière étape** : Utiliser le Kit 16.4 dans le TP du Jour 5.
