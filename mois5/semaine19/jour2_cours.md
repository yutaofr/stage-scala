---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 19"
footer: "Jour 2 — Docker Compose Complet"
---

# Orchestration de l'Écosystème
## Faire tourner tout le pays sur ton ordinateur

**Durée :** ~2h | **Fil Rouge :** Le "Stack" ATH complet

---

# 📋 Objectifs du Jour

- Harmoniser tous les services créés depuis 5 mois.
- Maîtriser les réseaux Docker (`networks`) et les volumes.
- Gérer l'ordre de démarrage (`depends_on` et `healthcheck`).
- Préparer un environnement "One-Click Deploy".

---

# 1. La vision globale

Ton système de clearing n'est pas qu'un JAR. C'est une symphonie :
- Kafka & Zookeeper (Transport).
- Cassandra (Persistance).
- Clearing Engine (Cœur).
- Prometheus & Grafana (Vues).
- Jaeger (Traces).

---

# 2. Le défi du "Ready"

Si le Moteur démarre avant que Kafka soit prêt, il va crasher.
L'option `depends_on` ne suffit pas car Kafka peut être "Vivant" mais pas encore "Prêt".

### Solution : Healthchecks
```yaml
services:
  kafka:
    healthcheck:
      test: ["CMD", "nc", "-z", "localhost", "9092"]
  engine:
    depends_on:
      kafka:
        condition: service_healthy
```

---

# 3. Variables d'Environnement

Ne jamais mettre d'IPs en dur dans ton code !
Utilise les noms de services Docker (`host: "kafka"`) et passe les secrets/config via le fichier `.env`.

---

# 🏗️ Application : Le Stack Complet v4.0

Nous allons construire un fichier `docker-compose.yml` héroïque. En tapant `docker-compose up`, tout le système de clearing démarrera, se connectera et commencera à monitorer les transactions automatiquement.

---

# 🧠 Quiz Rapide

1. Quel est l'avantage d'un réseau Docker dédié ? (Sécurité et résolution de noms DNS entre services).
2. À quoi sert le dossier `volumes` ? (À garder les données de Cassandra et Kafka même si on supprime les containers).
3. Pourquoi utiliser un fichier `.env` ? (Pour séparer la configuration du code de l'orchestrateur).

---

# 📝 Résumé du Jour

- Tu as créé un environnement de production miniature.
- Ton système est portable : il tournera de la même façon sur ton Mac que sur le serveur de la banque.
- Tu maîtrises l'orchestration de niveau intermédiaire.

**Prochaine étape** : Assembler le stack dans le TP 92 !
