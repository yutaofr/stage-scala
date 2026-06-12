# TP Jour 1 : Le Défi du Poids Plume

**Durée :** ~4h | **Fil Rouge :** Docker Multi-Stage Build

---

## Exercice 1 : Audit de l'existant (1h)

1. Build ton image actuelle : `docker build -t engine:fat .`.
2. Vérifie sa taille : `docker images`.
3. Liste les packages installés à l'intérieur : `docker exec engine:fat apk info`. Est-ce que tout est utile ?

---

## Exercice 2 : Implémentation Multi-Stage (2h)

1. Crée un nouveau `Dockerfile.optimized`.
2. Utilise une première étape `FROM hseeberger/scala-sbt` pour compiler le projet via `assembly`.
3. Utilise une seconde étape `FROM eclipse-temurin:17-jre-alpine` pour l'exécution.
4. Build et compare les tailles.

---

## Exercice 3 : Sécurité (1h)

1. Ajoute un utilisateur non-root dans ton Dockerfile (`USER 1000`).
2. Vérifie que tu peux toujours lancer l'application.
3. Pourquoi est-ce important de ne pas lancer de JAR en tant que `root` ?

**Livrable** : Nouveau `Dockerfile` multi-stage et comparatif des tailles d'images avant/après.
