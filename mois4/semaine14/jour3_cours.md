---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 3 — Gestion des Ressources (Scope)"
---

# ZIO Scope
## Sécuriser l'ouverture et la fermeture des fichiers

**Durée :** ~2h | **Fil Rouge :** Lecture sécurisée des batchs CSV

---

# 📋 Objectifs du Jour

- Comprendre le problème des fuites de mémoire (Memory Leaks).
- Utiliser le type `Scope`.
- Apprendre à utiliser `ZIO.acquireRelease` pour garantir le nettoyage.
- Gérer les fichiers et les connexions réseau sans peur.

---

# 1. Le Danger des fichiers ouverts

Si vous ouvrez 1000 fichiers sans les fermer, votre système d'exploitation finira par refuser d'en ouvrir de nouveaux.

### Pourquoi try/finally ne suffit pas ?
En programmation asynchrone, le "finally" peut s'exécuter avant que le traitement asynchrone ne soit terminé.

---

# 2. La Solution : acquireRelease

ZIO propose un mécanisme qui garantit que pour chaque "Acquisition" de ressource, il y aura une "Libération", peu importe si le programme réussit, échoue ou est interrompu.

```scala
val managedFile = ZIO.acquireRelease(
  ZIO.attempt(openFile("data.csv"))
)(file => ZIO.attempt(file.close()).orDie)
```

---

# 3. Utilisation Automatique (Scope)

ZIO utilise le canal **R** pour propager le besoin de nettoyage.

```scala
def processFile: ZIO[Scope, IOException, Unit] =
  for
    file <- managedFile
    _    <- readAndProcess(file)
  yield ()
```

> 💡 Dès que le programme sort du `Scope`, le fichier est fermé automatiquement. C'est le "Garbage Collection" des ressources externes.

---

# 🏗️ Application : Le Batch Reader

Nous allons créer un service qui lit un batch CSV volumineux. Nous utiliserons `Scope` pour garantir que le descripteur de fichier est libéré, même si une erreur de parsing survient au milieu du fichier.

---

# 🧠 Quiz Rapide

1. Une ressource est-elle libérée si le programme crashe ? (Oui, ZIO le garantit).
2. Puis-je utiliser une ressource en dehors de son `Scope` ? (Non, elle sera déjà fermée).
3. Pourquoi passer par `acquireRelease` plutôt qu'un simple `close()` manuel ? (Pour gérer les interruptions et les erreurs de manière déterministe).

---

# 📝 Résumé du Jour

- La gestion des ressources est cruciale pour la stabilité des serveurs (Uptime).
- ZIO `Scope` automatise cette gestion pénible.
- Ton moteur de clearing est maintenant "propre" : il ne laisse aucune trace derrière lui.
- Tu as acquis une compétence de développeur système senior.

**Prochaine étape** : Sécuriser tes accès fichiers dans le TP 68 !
