---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 4"
footer: "Jour 5 — Bilan Mois 1 & Architecture"
---

# Bilan du Mois 1
## Fondations de la Programmation Fonctionnelle

**Durée :** ~2h | **Fil Rouge :** Clearing Engine v0.4 — Prototype Complet

---

# 📋 Objectifs du Jour

- Récapituler les concepts clés du mois (Types, Immuabilité, HOF, Collections, Option).
- Analyser l'évolution de l'architecture du moteur de clearing.
- Comprendre les forces et limites des Tuples (avant de passer aux ADT).
- Préparer le passage au Mois 2 : Modélisation Métier Complexe.

---

# 1. Rétrospective Technique

### Semaine 1 & 2 : Syntaxe & Basics
- Variables immutables (`val`), Typage statique, Précision (`BigDecimal`).
- Expressions, Blocs, Tuples, Pattern Matching simple.

### Semaine 3 & 4 : Style Fonctionnel
- Collections immutables, pipelines (`map`, `filter`, `fold`).
- Fonctions d'ordre supérieur (HOF) et Currying.
- Abstraction avec les Traits.
- Sécurité avec `Option` et Récursion Terminale.

---

# 🏗️ Évolution de l'Architecture

### v0.1 (Semaine 1)
Script impératif simple avec des variables et des affichages.

### v0.2 (Semaine 2)
Introduction des traits et du parsing CSV basique.

### v0.3 (Semaine 3)
Calcul du netting via des pipelines de collections (Fini les boucles !).

### v0.4 (Aujourd'hui)
Gestion robuste des erreurs avec `Option`, règles de validation configurables par HOF.

---

# ⚠️ Le Problème des Tuples

Jusqu'ici, une transaction est un `(String, String, BigDecimal)`.
- Que signifie le premier `String` ? Source ? Destinataire ? On ne le sait pas à la lecture du type.
- On ne peut pas lui ajouter de comportements (méthodes).
- C'est ce qu'on appelle du "Primitive Obsession".

> 💡 Au Mois 2, nous apprendrons les **Case Classes** et les **Enums** pour créer une vraie modélisation métier.

---

# 🚀 En route vers le Mois 2

Le mois prochain sera consacré à :
1. **La Modélisation ADT** (Algebraic Data Types).
2. **Le système d'erreurs avancé** (`Either` et `Validated`).
3. **Les tests complexes** et le Property-Based Testing.
4. **Le démarrage du vrai projet** de compensation distribuée.

---

# 🧠 Grand Quiz du Mois

1. Pourquoi préférer `BigDecimal` à `Double` en banque ?
2. Quelle méthode de collection utiliser pour sommer des valeurs sans `var` ?
3. À quoi sert `@tailrec` ?
4. Quel est l'avantage de `Option` par rapport à une exception ?

---

# 📝 Conclusion du Mois 1

Félicitations ! Tu n'es plus un débutant en Scala. Tu as acquis l'état d'esprit fonctionnel : transformer des données immuables par des fonctions pures et sécurisées.

**Dernière étape** : Finaliser et démontrer ton prototype v0.4 !
