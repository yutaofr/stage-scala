# Semaine 20 : Livraison Finale (5 jours)

## Jour 1 — Polissage & Bugfix
**TP (journée complète)** : 
- Fix des derniers bugs identifiés lors de la pre-demo.
- Polissage du code : nommage, suppression du code mort, formatage.
- Vérification que tous les tests passent (unit + intégration + propriétés).

## Jour 2 — Préparation des Slides de Démonstration
**TP (journée complète)** :
- Créer les slides : Introduction → Problème → Architecture → Demo Live → Bilan.
- Préparer le scénario de démo pas-à-pas.
- Documenter les commandes à lancer pendant la demo.

## Jour 3 — Répétition Générale
**TP (journée complète)** :
- Scénario complet : Lancer l'infra Docker → Injecter des transactions → Observer le dashboard Grafana → Consulter l'API REST → Couper Kafka → Observer la reprise.
- Timing : viser 30 minutes de demo + 15 minutes de Q&A.

## Jour 4 — 🎉 GRANDE DÉMONSTRATION COMPLÈTE 🎉
**Devant l'équipe ATH :**
1. Présentation du contexte de compensation interbancaire.
2. Démonstration live du système distribué complet.
3. Injection de 10 000 transactions en temps réel.
4. Monitoring sur Grafana.
5. Simulation de panne et reprise automatique.
6. Questions-réponses.

## Jour 5 — Clôture du Stage
- **Knowledge Transfer** : Présentation du code et de l'architecture à l'équipe.
- **Archivage** : Tagging final sur Git, mise à jour du README.
- **Pot de départ** 🥂

---

## Évolution du Projet Fil Rouge (Résumé Complet)

| Version | Semaine | Acquis Principal |
|---|---|---|
| v0.1 | S1 | Prototype hardcodé (syntaxe de base) |
| v0.2 | S2 | Parsing CSV + Pattern Matching |
| v0.3 | S3 | Pipeline fonctionnel (`map`/`filter`/`fold`) |
| v0.4 | S4 | Gestion `Option` (plus de `null`) |
| v1.0 | S5 | ADTs (plus de tuples, plus de strings magiques) |
| v1.1 | S6 | Validation exhaustive avec reporting d'erreurs |
| v1.2 | S7 | Netting multilatéral N-banques |
| v1.3 | S8 | Interop Java/Spring |
| v2.0 | S9 | Code 100% pur (aucun effet de bord) |
| v2.1 | S10 | Erreurs typées (`Either`/`Try`), ne crashe jamais |
| v2.2 | S11 | Types opaques + sérialisation polymorphe |
| v2.3 | S12 | Testé par propriétés (ScalaCheck) |
| v3.0 | S15 | Streaming Kafka temps réel |
| v3.1 | S16 | Pipeline complet Kafka → ZIO → Cassandra |
| **FINAL** | S20 | Prod-ready : Monitoring, CI/CD, API REST, Haute Disponibilité |
