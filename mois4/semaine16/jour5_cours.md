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
- Cassandra pour le stockage Big Data et le reporting par banques.

---

# 🛡️ État des Lieux de la Solution (v3.1)

Votre application est désormais une véritable **Plateforme** :
- **Temps Réel** : Traitement instantané via Kafka.
- **Résilience** : Auto-réparatrice via les Acteurs et les Retries ZIO.
- **Persistance** : Archivage indestructible dans Cassandra.
- **Modularité** : Entièrement configurable via ZLayer.

---

# 🚀 Vers le Mois 5 : Industrialisation

On quitte le monde du développement pour celui de la **Production** :
1. **Tests d'Intégration** : Utiliser `TestContainers` pour automatiser les tests avec Kafka et Cassandra.
2. **CI/CD** : Builder l'image Docker avec `GitHub Actions` ou `SBT Native Packager`.
3. **Observabilité** : Monitorer le moteur avec `Prometheus` et `Grafana`.
4. **Kubernetes** : Déployer et scaler son moteur dans le Cloud.
5. **Projet Final** : Présentation devant le jury ATH.

---

# 🧠 Grand Quiz du Mois 4

1. Pourquoi ZIO est-il préférable aux Futures Java pour un projet de longue durée ?
2. Quel est l'avantage majeur de Kafka sur une file d'attente classique (RabbitMQ) ?
3. Pourquoi avoir choisi Cassandra plutôt que MySQL pour l'archivage ?
4. Comment as-tu géré la back-pressure (si la base est plus lente que le flux) ?

---

# 📝 Conclusion

Tu as franchi un cap immense. Tu ne codes plus des suites de commandes, tu designes des **Systèmes**. Cette expertise est ce qui différencie un développeur d'un architecte. Prépare-toi pour le mois final : celui de la mise en production réelle !

**Dernière étape** : Grande Démo v3.1 dans le TP 80 !
