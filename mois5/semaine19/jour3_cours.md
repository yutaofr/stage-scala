---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 19"
footer: "Jour 3 — CI/CD avec GitHub Actions"
---

# Automatisation CI/CD
## Du code au déploiement sans intervention humaine

**Durée :** ~2h | **Fil Rouge :** Le pipeline de livraison continue d'ATH

---

# 📋 Objectifs du Jour

- Comprendre le cycle **Intégration Continue (CI)** et **Déploiement Continu (CD)**.
- Découvrir la syntaxe YAML de **GitHub Actions**.
- Créer un workflow qui compile, teste et build l'image Docker à chaque `git push`.
- Apprendre à gérer les secrets (clés API, mots de passe de registre).

---

# 1. Pourquoi automatiser ?

- **Qualité** : On ne peut plus "oublier" de lancer les tests. Le serveur refuse le code si un test échoue.
- **Vitesse** : L'image Docker est créée et poussée sur le registre automatiquement en 5 minutes.
- **Confiance** : On sait que ce qui est en production correspond exactement au code master.

---

# 2. GitHub Actions en 3 mots

1. **Workflow** : Le processus global (fichier `.github/workflows/main.yml`).
2. **Job** : Une étape du processus (ex: Build, Test, Publish).
3. **Step** : Une commande spécifique à l'intérieur d'un job.

```yaml
jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Tests
        run: sbt test
```

---

# 3. Sécurité des Secrets

Ne place pas de mot de passe dans le YAML. Utilise les secrets du dépôt ou de l'environnement.
Ils sont injectés dans le pipeline via des variables d'environnement sécurisées.

---

# 🏗️ Application : Le pipeline de livraison

Nous allons écrire le fichier de configuration qui permettra à ATH d'être à la pointe de l'industrie :
- Sur chaque pull request :
  - Lancement des tests unitaires.
  - Lancement des tests d'intégration.
  - Build de l'image Docker multi-stage.
- Sur un tag ou une branche autorisée :
  - Publication de l'image après succès des contrôles.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre CI et CD ? (CI = Intégration/Tests, CD = Déploiement/Livraison).
2. Quel fichier définit un workflow GitHub Actions ? (Un fichier `.yml` dans `.github/workflows/`).
3. Peut-on faire tourner des containers Docker à l'intérieur de GitHub Actions ? (Oui, c'est idéal pour les tests d'intégration).

---

# 📝 Résumé du Jour

- Le pipeline CI/CD est l'usine de ton application.
- GitHub Actions simplifie l'automatisation en restant proche du code.
- L'automatisation rend les contrôles répétables, mais le développeur reste responsable du diagnostic et de la décision de livraison.

**Prochaine étape** : Utiliser le Kit 19.3 dans le TP du Jour 3.
