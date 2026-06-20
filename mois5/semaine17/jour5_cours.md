---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 17"
footer: "Jour 5 — Alerting & SLOs"
---

# Alerting & SLOs
## Dormir sereinement pendant que le système surveille

**Durée :** ~2h | **Fil Rouge :** Ne pas rater un crash de production

---

# 📋 Objectifs du Jour

- Comprendre la notion de **SLI** (Indicateur) et **SLO** (Objectif).
- Configurer des règles d'alerte dans Prometheus/Alertmanager.
- Apprendre à différencier une "Alerte Critique" d'une "Notification".
- Rendre le système de clearing capable d'appeler à l'aide tout seul.

---

# 1. Les Termes du SRE (Google)

- **SLI (Service Level Indicator)** : Ce que l'on mesure (ex: Taux de succès).
- **SLO (Service Level Objective)** : La cible (ex: 99.9% de succès).
- **Error Budget** : la part d'indisponibilité ou d'échec compatible avec le SLO sur une période.

---

# 2. Alertmanager

Prometheus détecte une anomalie (ex: Erreurs > 5%), mais c'est **Alertmanager** qui décide comment prévenir l'humain.
- Email.
- Slack / Microsoft Teams.
- PagerDuty (Appel téléphonique).

---

# 3. Éviter la "Fatigue d'Alerte"

Une alerte ne doit se déclencher que si :
1. C'est **vrai** (Pas un faux positif).
2. C'est **urgent** (Il faut agir maintenant).
3. C'est **actionnable** (On sait quoi faire pour réparer).

> [!CAUTION]
> Trop d'alertes inutiles = Les développeurs finissent par les ignorer.

---

# 🏗️ Application : L'Alerte "Système KO"

Nous allons configurer une alerte d'absence de cible et une alerte de taux d'erreur. Les seuils du laboratoire servent à vérifier le mécanisme ; un seuil de production doit découler d'un SLO et d'une fenêtre.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre SLI et SLO ? (Le SLI est la mesure réelle, le SLO est l'objectif à atteindre).
2. Pourquoi ne pas envoyer une alerte pour chaque erreur individuelle ? (Pour éviter la saturation et parce que des erreurs isolées sont normales dans un système distribué).
3. À partir de quel outil configure-t-on l'envoi d'emails ? (Alertmanager).

---

# 📝 Résumé de la Semaine 17

- Tu as rendu ton application **Professionnelle**.
- Elle produit des logs JSON, expose des métriques, génère des traces et surveille ses propres objectifs (SLOs).
- Tu peux maintenant dire avec certitude : "Mon système fonctionne et je le prouve avec Grafana".

**Prochaine étape** : Utiliser le Kit 17.5 dans le TP du Jour 5.
