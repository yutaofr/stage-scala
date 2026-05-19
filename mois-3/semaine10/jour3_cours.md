---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 10"
footer: "Jour 3 — Railway Oriented Programming (ROP)"
---

# Railway Oriented Programming
## Visualiser le flux de succès et d'erreur

**Durée :** ~2h | **Fil Rouge :** Architecture "Double Rail" du Clearing Engine

---

# 📋 Objectifs du Jour

- Comprendre le concept de ROP théorisé par Scott Wlaschin.
- Visualiser le code comme deux rails parallèles (Success/Failure).
- Apprendre à "aiguiller" les données entre les rails.
- Maîtriser les briques de base du ROP : map, flatMap, bimap.

---

# 1. Le Concept du Double Rail

Un pipeline FP n'est pas une ligne droite, c'est un chemin de fer à deux voies.

```text
Entrée --> [ Étape 1 ] --Succès--> [ Étape 2 ] --Succès--> Sortie
              |                     |
              v                     v
           Échec                 Échec
              |                     |
              +-- Rail d'Erreur ----+-------------------> Rapport
```

- Le rail du haut : La voie heureuse (Happy Path).
- Le rail du bas : La voie des erreurs (Failure Path).

---

# 2. Les Aiguillages (Adaptateurs)

Toutes les briques n'ont pas la même forme. Il faut les adapter au rail.

- **Map (1-track to 1-track)** : Transforme la donnée sans risque d'erreur.
- **FlatMap (1-track to 2-track)** : Peut bifurquer vers le rail d'erreur.
- **Bimap (2-track to 2-track)** : Transforme le succès **OU** l'erreur.

---

# 3. Fold : Gérer les deux mondes

`fold` permet de transformer les deux côtés d'un `Either` en une seule valeur.

```scala
result.fold(
  error => s"Audit Error: ${error.msg}", // Cas Left
  tx    => s"Audit Success: ID ${tx.id}" // Cas Right
)
```

> ⚠️ **Note Importante :** Le cours mentionne `bimap`, mais cette méthode n'existe **PAS** sur `Either` en Scala standard. C'est une méthode de librairies comme Cats ou Scalaz.
> En Scala standard, **`fold`** est l'outil correct pour transformer les deux rails en une seule valeur. Il est plus puissant que `bimap` car il unifie les deux types de retour.

> 💡 C'est l'outil ultime pour le reporting final ou le logging structuré.

---

# 🏗️ Application : Le Rail d'ATH

Notre moteur est maintenant un pur "Railway System". Chaque micro-fonction est un rail qui se branche sur le précédent.

- Si tout est `Right`, on arrive au netting.
- Si un seul `Left` apparaît, la donnée "tombe" sur le rail du bas et y reste jusqu'à l'affichage final.

---

# 🧠 Quiz Rapide

1. Que se passe-t-il pour une donnée qui est tombée sur le rail d'erreur ? (Elle y reste, les fonctions suivantes du rail de succès sont ignorées).
2. Quelle fonction permet de faire remonter une donnée du rail d'erreur vers celui du succès ? (C'est rare, mais possible via `recover` ou `orElse`).
3. Quelle est la différence entre `fold` et `map` ? (`map` ne transforme que le `Right`, `fold` transforme les deux côtés en une seule valeur).

---

# 📝 Résumé du Jour

- Le ROP est un style d'architecture monadique visuel.
- On ne gère pas les erreurs, on les laisse couler sur leur propre rail.
- Le code devient un assemblage d'aiguillages (`flatMap`).
- `fold` permet de consommer les deux rails dans un reporting unifié.
- Tu as maintenant une vision architecturale de ton moteur de clearing.

**Prochaine étape** : Dessiner et coder ton rail dans le TP 48 !
