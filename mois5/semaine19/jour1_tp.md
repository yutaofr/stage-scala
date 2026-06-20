# TP Jour 1 : Le Défi du Poids Plume

**Durée :** ~4h | **Fil Rouge :** Docker Multi-Stage Build

---

## Point de départ

- Copie le **Kit 19.1** de `starter_kit_s19.md`.
- Utilise les versions Scala/SBT du projet au lieu d'un tag d'image pris au hasard.
- Compare taille, utilisateur, couches et démarrage.

## Exercice 1 : Audit de l'existant (1h)

1. Build ton image actuelle : `docker build -t engine:fat .`.
2. Vérifie sa taille : `docker images`.
3. Inspecte les couches avec `docker history engine:fat`.
4. Vérifie l'utilisateur avec `docker run --rm engine:fat id`.
5. Note si un shell ou un gestionnaire de paquets est réellement présent.

**Validation :** le tableau initial contient taille, nombre de couches, utilisateur et temps de démarrage.

---

## Exercice 2 : Implémentation Multi-Stage (2h)

1. Adapte le Dockerfile multi-stage fourni.
2. Compile l'assembly dans l'étape builder.
3. Copie uniquement le JAR et les fichiers nécessaires dans l'étape runtime.
4. Ajoute un `.dockerignore`.
5. Build et compare les tailles et le démarrage.

**Validation :** l'image finale ne contient ni sources ni cache SBT.

---

## Exercice 3 : Sécurité (1h)

1. Crée un utilisateur système non-root.
2. Donne-lui seulement les droits nécessaires.
3. Vérifie `id`, lecture du JAR et écriture dans le seul répertoire prévu.
4. Lance un scan de vulnérabilités si l'outil est disponible.

**Validation :** le processus Java n'est pas root et l'application démarre.

**Livrable** : Nouveau `Dockerfile` multi-stage et comparatif des tailles d'images avant/après.
