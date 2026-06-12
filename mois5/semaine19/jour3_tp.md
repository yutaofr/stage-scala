# TP Jour 3 : L'Usine à Logiciels

**Durée :** ~4h | **Fil Rouge :** Pipeline GitHub Actions

---

## Exercice 1 : Mon premier Workflow (1h)

1. Crée le dossier `.github/workflows/` à la racine de ton projet.
2. Crée un fichier `ci.yml`.
3. Configure-le pour se déclencher sur chaque `push`.
4. Ajoute une étape qui affiche "Build en cours..." et la version de Java.

---

## Exercice 2 : Tests Automatisés (1h30)

1. Utilise l'action `actions/setup-java`.
2. Ajoute l'étape `sbt test`.
3. Fais un commit avec un test qui échoue et vérifie sur GitHub que le pipeline passe au rouge.
4. Corrige le test et vérifie qu'il repasse au vert.

---

## Exercice 3 : Build Docker Auto (1h30)

1. Utilise l'action `docker/build-push-action`.
2. Configure le workflow pour builder ton image optimisée (S19-J1).
3. (Optionnel) Configure la publication vers un registre fictif.

**Livrable** : Fichier `ci.yml` complet et lien (ou capture d'écran) vers l'historique des builds réussis.
