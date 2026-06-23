---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 4, Semaine 14"
footer: "Jour 3 — Gestion des Ressources (Scope)"
---

# ZIO Scope
## Sécuriser l'ouverture et la fermeture des fichiers

**Durée :** ~2h | **Fil Rouge :** Audit en mémoire d'un batch de clearing

---

# 📋 Objectifs du Jour

- Comprendre le problème des fuites de ressources.
- Utiliser le type `Scope`.
- Apprendre à utiliser `ZIO.acquireRelease` pour garantir le nettoyage.
- Observer une ressource courte sans construire un vrai parseur de fichier.

---

# 1. Le Danger des fichiers ouverts

Si vous ouvrez 1000 fichiers sans les fermer, votre système d'exploitation finira par refuser d'en ouvrir de nouveaux.

### Pourquoi le scope est préférable ?
Un `try/finally` bien placé peut fermer une ressource, mais il se compose mal avec l'interruption et les traitements concurrents. Un scope rattache explicitement la durée de vie de la ressource à celle de l'effet.

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

> 💡 Dès que le programme sort du `Scope`, le finaliseur est exécuté. Ce mécanisme est déterministe et distinct du Garbage Collector.

---

# 🏗️ Application : L'audit du batch personnel

Nous allons lire en mémoire quelques lignes construites depuis le batch de référence du stagiaire. Le but est de voir `acquireRelease` et `ZIO.scoped`, pas de construire un batch reader complet.

---

# 🧠 Quiz Rapide

1. Une ressource est-elle libérée si l'effet échoue ou si sa Fiber est interrompue ? (Oui, si elle a été acquise dans le scope).
2. Puis-je utiliser une ressource en dehors de son `Scope` ? (Non, elle sera déjà fermée).
3. Pourquoi passer par `acquireRelease` plutôt qu'un simple `close()` manuel ? (Pour gérer les interruptions et les erreurs de manière déterministe).

---

# 📝 Résumé du Jour

- La gestion des ressources est cruciale pour la stabilité des serveurs (Uptime).
- ZIO `Scope` automatise cette gestion pénible.
- Le module d'observation montre comment borner proprement une ressource.
- Le cœur Scala de base reste inchangé.

**Prochaine étape** : Observer le même module unique dans le TP du Jour 3.
