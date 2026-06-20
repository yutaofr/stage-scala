---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 19"
footer: "Jour 5 — Pre-Demo & Revue Industrielle"
---

# Bilan Semaine 19
## L'Application est Prête pour le Monde

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v4.0 — Le Standard Industriel

---

# 📋 Objectifs du Jour

- Récapituler tout ce qui fait d'un code un "Produit".
- Vérifier la chaîne complète : Git -> CI/CD -> Docker -> Stack -> Monitoring.
- Préparer la soutenance finale du mois 5.
- Identifier les derniers petits bugs de polissage.

---

# 1. Qu'est-ce qu'une "Revue Industrielle" ?

Avant de livrer à la banque ATH, on coche les cases :
- [ ] Le build passe en CI.
- [ ] L'image Docker est légère.
- [ ] La documentation Swagger est à jour.
- [ ] Les métriques sont visibles dans Grafana.
- [ ] Le code est propre et commenté.

---

# 2. Le Rôle du Product Owner

On vérifie que les besoins métiers (le clearing) sont toujours respectés malgré la complexité technique accumulée.
- Le netting est-il toujours juste ?
- Les doublons sont-ils toujours gérés ?

---

# 3. Préparer la Démo

Une bonne démo n'est pas une lecture de code. C'est un récit :
1. "Voici la situation : des banques envoient des milliers de virements."
2. "Regardez Kafka les transporter."
3. "Voyez le moteur les traiter en direct (Dashboard Grafana)."
4. "Vérifiez que tout est sauvé dans Cassandra."

---

# 🏗️ Application : La Version 4.0

Nous allons préparer un candidat `v4.0.0-rc1`. Le tag final ne sera créé qu'après les contrôles et la soutenance de la S20.

---

# 🧠 Quiz Rapide

1. Puis-je livrer un code sans tests automatisés ? (Non, c'est une faute professionnelle).
2. À quoi sert le tag Git `v4.0.0` ? (À marquer une étape majeure et immuable du projet).
3. Quel est l'outil le plus impressionnant à montrer à un client non-technique ? (Le dashboard Grafana ou le Swagger interactif).

---

# 📝 Résumé de la Semaine 19

- Tu as automatisé tout ton travail.
- Ton application est un package professionnel, prêt à être installé chez n'importe quel client.
- Tu as appris à documenter et à sécuriser ton infrastructure.
- Félicitations, tu viens de terminer le cycle complet de développement logiciel.

**Prochaine étape** : Utiliser le Kit 19.5 dans le TP du Jour 5.
