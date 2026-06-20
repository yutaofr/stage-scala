---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 19"
footer: "Jour 4 — Documentation OpenAPI avec Tapir"
---

# API Documentation
## Rendre son service compréhensible par tous

**Durée :** ~2h | **Fil Rouge :** Un Swagger pour le Clearing de la Banque Centrale

---

# 📋 Objectifs du Jour

- Comprendre l'importance d'une documentation API vivante.
- Découvrir la bibliothèque **Tapir** (Type-safe API Endpoints).
- Générer automatiquement un fichier **OpenAPI** (Swagger) depuis le code.
- Exposer une interface interactive pour tester le clearing.

---

# 1. Pourquoi documenter ?

Une API sans doc est une API inutilisable.
- Les autres banques ont besoin de savoir quels champs envoyer.
- **OpenAPI** (Swagger) est le langage universel pour décrire les APIs REST.

---

# 2. Tapir : décrire une API avec des valeurs Scala

Tapir permet de décrire tes endpoints avec des types Scala purs.
```scala
val ingestEndpoint = endpoint.post
  .in("ingest")
  .in(jsonBody[Transaction])
  .out(stringBody)
```
- **Sécurité** : Si tu changes le type `Transaction`, la doc change toute seule.
- **Modularité** : la même description peut être interprétée en serveur, client et documentation, selon les modules choisis.

---

# 3. Swagger UI

Le Graal pour les développeurs front-end. Une page web interactive où ils peuvent :
1. Lire la liste des routes.
2. Voir les exemples de JSON.
3. Cliquer sur "Try it out" pour envoyer une vraie transaction.

---

# 🏗️ Application : Le Portail Développeur ATH

Nous allons exposer une route `/docs` sur notre moteur de clearing. Elle affichera le Swagger UI complet de notre système, permettant à n'importe quel stagiaire ou ingénieur d'ATH de tester les validations et les calculs de netting sans écrire une ligne de code.

---

# 🧠 Quiz Rapide

1. Que signifie l'acronyme OpenAPI ? (C'est le standard de description d'APIs).
2. Pourquoi Tapir est-il plus "Scala" que les annotations Java ? (Car il utilise des types et des valeurs pures plutôt que des métadonnées magiques au-dessus des classes).
3. À quoi sert le Swagger UI ? (À tester l'API visuellement dans un navigateur).

---

# 📝 Résumé du Jour

- Ton code est maintenant sa propre documentation.
- Tu offres une expérience "Premium" aux autres développeurs.
- Ton application est prête à être intégrée dans le Système d'Information Global.

**Prochaine étape** : Utiliser le Kit 19.4 dans le TP du Jour 4.
