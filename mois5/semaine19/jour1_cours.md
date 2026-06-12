---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 5, Semaine 19"
footer: "Jour 1 — Docker Multi-Stage & Images Optimisées"
---

# Dockerisation Avancée
## Produire des images légères et sécurisées

**Durée :** ~2h | **Fil Rouge :** Réduire la taille de l'image de 1 Go à < 200 Mo

---

# 📋 Objectifs du Jour

- Comprendre pourquoi une image Docker lourde est un problème.
- Maîtriser le **Multi-Stage Build**.
- Choisir la bonne image de base (Alpine, Distroless).
- Isoler les dépendances de compilation du binaire final.

---

# 1. Pourquoi des images légères ?

- **Vitesse** : Plus l'image est petite, plus elle se télécharge et se déploie vite.
- **Sécurité** : Moins il y a d'outils (bash, curl, etc.) dans l'image, moins un pirate a de leviers pour attaquer.
- **Coût** : Moins de stockage utilisé dans le cloud.

---

# 2. Le Multi-Stage Build

On utilise deux images dans un seul `Dockerfile` :
1. Une image lourde avec Java + SBT pour **compiler**.
2. Une image ultra-légère avec juste le JRE (ou même rien) pour **exécuter**.

```dockerfile
# Stage 1: Build
FROM sbt-image AS builder
COPY . .
RUN sbt assembly

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
COPY --from=builder /app/target/clearing-engine.jar .
CMD ["java", "-jar", "clearing-engine.jar"]
```

---

# 3. Alpine vs Distroless

- **Alpine** : Très petite (~5 Mo), mais utilise `musl` au lieu de `glibc` (parfois des bugs bizarres en Java).
- **Distroless** (Google) : Plus sécurisée, elle ne contient AUCUN shell. On ne peut même pas faire `docker exec`. C'est le top pour la prod.

---

# 🏗️ Application : La Cure d'Amincissement

Nous allons transformer notre `Dockerfile` actuel. Nous allons passer d'une image de 800 Mo contenant tout le code source et les caches SBT à une image de 150 Mo contenant uniquement le JAR compilé et un JRE optimisé.

---

# 🧠 Quiz Rapide

1. À quoi sert le mot-clé `AS` dans un Dockerfile ? (A nommer une étape pour pouvoir y faire référence plus tard).
2. Est-il utile de garder le code source (`.scala`) dans l'image finale ? (Non, seul le `.jar` est nécessaire).
3. Pourquoi privilégier les images "alpine" ? (Pour leur taille extrêmement réduite).

---

# 📝 Résumé du Jour

- Multi-stage = Séparation des responsabilités.
- Une image de production doit être un "caillou" (inerte et solide).
- Tu as appris à diviser par 5 le temps de déploiement de ton moteur.

**Prochaine étape** : Alléger ton image dans le TP 91 !
