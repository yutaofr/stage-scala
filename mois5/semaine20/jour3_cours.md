---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 20"
footer: "Jour 3 — Scénario de Démo Live"
---

# La Démo Live
## Le moment de vérité

**Durée :** ~2h | **Fil Rouge :** Scénario : "Une journée à la BCP"

---

# 📋 Objectifs du Jour

- Écrire le script détaillé de sa démonstration.
- Anticiper l'effet Bonaldi (rien ne marche le jour J).
- Préparer des "Sauvegardes" (Captures d'écran au cas où).
- Répéter le timing pour ne pas dépasser 10 minutes.

---

# 1. Le Scénario "A Path"

1. **Clean Slate** : Montrer que Cassandra et Grafana sont vides.
2. **Ignition** : Lancer le Moteur de Clearing.
3. **Traffic** : Lancer le Simulateur (500 transactions).
4. **Visibility** : Montrer les graphes qui montent dans Grafana.
5. **Quality** : Ouvrir Swagger et montrer qu'on peut requêter les soldes.
6. **Resilience** : Couper un moteur et montrer que ça continue.

---

# 2. La Gestion du Stress

- Parle lentement.
- Explique ce que tu fais **avant** de le faire.
- Si un bug arrive : "C'est l'essence même d'un système distribué, voyons comment Cassandra gère cette interruption...". Transforme l'erreur en leçon.

---

# 3. La Conclusion Impactante

Termine sur les chiffres : "En 5 mois, nous sommes passés d'un calcul manuel à une plateforme capable de traiter 10 000 transactions par seconde avec une sécurité mathématique totale."

---

# 🏗️ Application : Le Répétiteur

Nous allons faire tourner la démo 3 fois de suite :
1. Une fois pour vérifier la technique.
2. Une fois pour vérifier le discours.
3. Une fois pour se chronométrer.

---

# 🧠 Quiz Rapide

1. Pourquoi commencer par montrer un système vide ? (Pour prouver qu'il n'y a pas de triche et que tout se passe en direct).
2. Que faire si le réseau coupe pendant la démo ? (Passer sur les slides de "sauvegarde" préparées à l'avance).
3. Quel est le but d'une démo ? (Prouver que le produit répond au besoin de manière sexy et robuste).

---

# 📝 Résumé du Jour

- La démo est un spectacle technique.
- Tu maîtrises ton sujet sur le bout des doigts.
- Demain... c'est le grand jour.

**Prochaine étape** : Ton script de démo dans le TP 98 !
