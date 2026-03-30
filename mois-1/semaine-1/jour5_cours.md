---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 1"
footer: "Jour 5 — Synthèse & Premier Prototype"
---

# Synthèse & Premier Prototype
## Assembler les briques de la semaine

**Durée :** ~2h | **Fil Rouge :** Ton premier moteur de compensation v0.1

---

# 📋 Objectifs du Jour

- Réviser les concepts clés de la semaine.
- Valider les bonnes pratiques de code (Immuabilité, Nommage).
- Comprendre l'architecture globale du projet fil rouge.
- Préparer la transition vers la modélisation avancée.

---

# 1. Revue de Code (Checklist)

Avant de passer à la suite, on vérifie la qualité :

- ✅ **Immuabilité** : Aucun `var` n'a survécu ?
- ✅ **Nommage** : Les variables sont-elles explicites (ex: `montantNet` vs `m`) ?
- ✅ **Types** : Les signatures des fonctions sont-elles claires et annotées ?
- ✅ **Tests** : Est-ce que tes fonctions sont couvertes par des tests unitaires ?

---

# ⌨️ Ton Poste de Travail : IntelliJ

Maîtriser son IDE = Gagner du temps de réflexion.

| Raccourci (Mac) | Action |
|---|---|
| `⌘ + Shift + L` | Reformater le code (Standard Scala) |
| `⌘ + B` | Naviguer vers la définition d'un type |
| `⌘ + Alt + V` | Extraire une variable |
| `Ctrl + Shift + R` | Lancer les tests du fichier courant |

---

# 🏗️ Architecture Globale du Projet

Le système final sera un pipeline de données complet.

```text
  [ INGESTION ]  ──▶  [ MOTEUR DE COMP ]  ──▶  [ STOCKAGE ]
  (Kafka / CSV)       (Scala Fonctionnel)     (Cassandra)
        ▲                      ▲                    ▲
      Mois 4                Mois 1-3              Mois 5
```

---

# 📦 Tes Briques de la Semaine

Tu as déjà construit les fondations :
- `Basics.scala` : Types de base.
- `CurrencyConverter.scala` : Logique mathématique.
- `Validator.scala` : Logique conditionnelle.
- `NettingCalculator.scala` : Algorithme de calcul.

---

# 🧠 Quiz de Fin de Semaine

- Quel type permet d'éviter les erreurs d'arrondi monétaire ?
- Pourquoi préfère-t-on `val` à `var` ?
- Que fait le mot-clé `yield` dans une boucle `for` ?

---

# ➡️ La Semaine Prochaine

**Semaine 2 : Structures de Contrôle & Pattern Matching Avancé**

Nous allons voir :
- Comment le Pattern Matching permet de manipuler les données en sécurité.
- Les For-Comprehensions imbriquées.
- Introduction aux collections immuables complexes.

**Bravo pour cette première semaine !** 🎉
