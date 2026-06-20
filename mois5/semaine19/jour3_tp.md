# TP Jour 3 : L'Usine à Logiciels

**Durée :** ~4h | **Fil Rouge :** Pipeline GitHub Actions

---

## Point de départ

- Copie le **Kit 19.3** de `starter_kit_s19.md`.
- Travaille sur une branche de test avant d'activer la publication.
- N'accorde que les permissions nécessaires au workflow.

## Exercice 1 : Mon premier Workflow (1h)

1. Crée le dossier `.github/workflows/` à la racine de ton projet.
2. Crée un fichier `ci.yml`.
3. Déclenche-le sur `pull_request` et `push` vers `main`.
4. Affiche les versions Java, SBT et Scala.
5. Ajoute `concurrency` pour annuler un ancien run de la même branche.

**Validation :** un second push annule le run devenu obsolète.

---

## Exercice 2 : Tests Automatisés (1h30)

1. Utilise `actions/setup-java@v4` avec cache SBT.
2. Ajoute `sbt clean test`.
3. Fais un commit avec un test qui échoue et vérifie sur GitHub que le pipeline passe au rouge.
4. Corrige le test et vérifie qu'il repasse au vert.

**Validation :** la preuve contient le run rouge puis le run vert.

---

## Exercice 3 : Build Docker Auto (1h30)

1. Utilise Buildx et `docker/build-push-action`.
2. Build l'image sur les pull requests sans la pousser.
3. Active la publication uniquement sur un tag `v*`.
4. Utilise un secret de registre et des permissions minimales.

**Validation :** une pull request ne publie aucune image ; un tag autorisé produit un tag immuable.

**Livrable** : Fichier `ci.yml` complet et lien (ou capture d'écran) vers l'historique des builds réussis.
