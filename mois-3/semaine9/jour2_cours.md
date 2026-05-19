---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 9"
footer: "Jour 2 — Fonctions Pures & Immuabilité"
---

# Fonctions Pures
## Écrire du code sans "surprises"

**Durée :** ~2h | **Fil Rouge :** Un NettingCalculator mathématique

---

# 📋 Objectifs du Jour

- Définir rigoureusement une **Fonction Pure**.
- Éliminer toute mutation d'état (`var`) résiduelle.
- Apprendre les bénéfices du déterminisme.
- Comprendre pourquoi l'immuabilité facilite le raisonnement.

---

# 1. Qu'est-ce qu'une fonction Pure ?

Une fonction est pure si :
1. Elle retourne toujours le même résultat pour les mêmes arguments (**Déterminisme**).
2. Elle n'a aucun effet de bord (I/O, Mutation, Exception).

### Exemple Pur ✅
```scala
def add(a: Int, b: Int): Int = a + b
```

### Exemple Impur ❌
```scala
var total = 0
def addAndAccumulate(a: Int): Int = { 
  total += a // Mutation d'état global
  total 
}
```

---

# 2. L'Immuabilité Totale

Pour garantir la pureté, on bannit le mot-clé `var`.

### Pourquoi ?
- **Threading** : Aucun risque de "Race Condition" (plusieurs threads modifiant la même donnée).
- **Testabilité** : Pas besoin de "remettre à zéro" l'état avant chaque test.
- **Simplicité** : Tu n'as pas besoin de garder en tête "la valeur actuelle" d'une variable au fil du temps.

---

# 3. Transformer les Boucles en Pureté

Au lieu de changer une variable dans une boucle `while`, on utilise des transformations de collections (`map`, `fold`).

### Impur (Style Java)
```scala
var sum = 0
for (x <- list) sum += x
```

### Pur (Style Scala)
```scala
val sum = list.foldLeft(0)(_ + _)
```

---

# 🏗️ Application : NettingCalculator v2.0

Nous allons réécrire le calculateur de positions nettes. Il prendra une `List[Transaction]` et renverra une `Map[String, BigDecimal]`.

- Aucun `var`.
- Aucun `println` interne.
- Le calcul est une pure transformation de données.

---

# 🧠 Quiz Rapide

1. Une fonction qui lit une ligne dans un fichier est-elle pure ? (Non, I/O).
2. Pourquoi `foldLeft` est-il considéré comme pur ? (Il ne modifie pas la liste d'origine, il crée un résultat final).
3. Quel est le principal avantage de l'immuabilité pour le débogage ? (On sait que les objets ne changent pas de valeur "dans notre dos").

---

# 📝 Résumé du Jour

- Les fonctions pures sont les briques de base de la FP.
- L'immuabilité élimine des classes entières de bugs (concurrence, état corrompu).
- Chaque calcul devient une transformation de données prédictible.
- Ton moteur de clearing devient un "moteur mathématique".

**Prochaine étape** : Réécrire ton calculateur en pur dans le TP 42 !
