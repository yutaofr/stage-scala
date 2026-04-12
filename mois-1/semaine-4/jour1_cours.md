---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 4"
footer: "Jour 1 — HOF Avancées & Currying"
---

# HOF Avancées & Currying
## Modulariser le code par les fonctions

**Durée :** ~2h | **Fil Rouge :** Règles de validation configurables

---

# 📋 Objectifs du Jour

- Comprendre les fonctions d'ordre supérieur (**HOF**).
- Apprendre à passer des fonctions en paramètres.
- Maîtriser le **Currying** pour la configuration partielle.
- Créer un moteur de règles flexible.

---

# 1. Qu'est-ce qu'une HOF ?

Une Higher-Order Function est une fonction qui :
1. Prend une fonction en paramètre.
2. OU retourne une fonction.

### Exemple : Filtrage sur mesure
```scala
def verifier(montant: BigDecimal, regle: BigDecimal => Boolean): Boolean =
  regle(montant)

val estGrosMontant = (m: BigDecimal) => m > 10000

verifier(5000, estGrosMontant) // false
```

---

# 2. Le Currying

Le Currying consiste à transformer une fonction à plusieurs paramètres en une suite de fonctions à un seul paramètre.

```scala
// Fonction classique
def add(a: Int, b: Int) = a + b

// Fonction curryfiée
def addCurry(a: Int)(b: Int) = a + b

val addFive = addCurry(5) // C'est une fonction !
addFive(10) // 15
```

---

# 🏗️ Application : Configuration Partielle

Idéal pour injecter une configuration (ex: devise) une seule fois.

```scala
def transformer(taux: BigDecimal)(montant: BigDecimal): BigDecimal =
  montant * taux

val deEuroVersMad = transformer(10.85)

// On l'utilise ensuite partout sans répéter le taux
deEuroVersMad(100)
deEuroVersMad(250)
```

---

# 3. Combiner les Fonctions

Le langage fonctionnel permet de composer des règles complexes.

```scala
val regles = List(
  (m: BigDecimal) => m > 0,
  (m: BigDecimal) => m < 1000000
)

// Est-ce que TOUTES les règles sont respectées ?
regles.forall(f => f(BigDecimal("500"))) // true
```

---

# 🧠 Quiz Rapide

1. Donnez une définition simple d'une HOF.
2. À quoi sert le Currying en pratique ?
3. Quelle méthode de liste permet de vérifier que TOUS les éléments respectent une règle ?

---

# 📝 Résumé du Jour

- Les HOF permettent d'injecter du comportement dans les fonctions.
- Le Currying facilite la création de fonctions spécialisées (configuration partielle).
- Les listes de fonctions permettent de construire des moteurs de règles dynamiques et extensibles.

**Prochaine étape** : Créer un validateur configurable dans le TP 15 !
