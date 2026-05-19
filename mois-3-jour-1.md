---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 9"
footer: "Jour 1 — Transparence Référentielle"
---

# Transparence Référentielle
## La base de la prédictibilité en informatique

**Durée :** ~2h | **Fil Rouge :** Isoler les effets de bord du Clearing Engine

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Transparence Référentielle** (RT).
- Identifier les **Effets de Bord** (Side Effects) cachés.
- Apprendre à séparer la logique pure de l'exécution impure.
- Rendre le code du moteur mathématiquement prouvable.

---

# 1. Qu'est-ce que la RT ?

Une expression est dite "Transparent Référentiellement" si on peut la remplacer par son résultat sans changer le comportement du programme.

### Exemple Pur
```scala
val x = 2 + 2
val y = x + x // On peut remplacer x par 4 -> y = 4 + 4
```

### Exemple Impur (Effet de bord)
```scala
def inc(a: Int): Int = { println("Log"); a + 1 }
val x = inc(2)
val y = x + x // Si on remplace x par 3, on ne voit plus le "Log" !
```

---

# 2. Les Effets de Bord Cachés

En banque, les effets de bord sont partout :
- `println` (I/O).
- Lire un fichier CSV.
- Appeler une API de taux de change.
- Lever une exception (`throw`).
- Modifier une variable globale (`var`).

> [!WARNING]
> Les effets de bord rendent le code difficile à tester et imprévisible en cas de parallélisation.

---

# 3. Séparation : "Push the Side Effects to the Edges"

La stratégie gagnante :
1. **Cœur du moteur** : 100% Pur (RT). Reçoit des données, renvoie des données.
2. **Couche d'entrée/sortie** : Gère les effets (Lecture CSV, Logs).

```scala
// PUR ✅
def calculateNet(txs: List[Transaction]): BigDecimal = ...

// IMPUR ❌
def calculateAndLog(txs: List[Transaction]): Unit = { 
  val res = calculateNet(txs)
  println(s"Result: $res")
}
```

---

# 🏗️ Application au Clearing

Nous allons extraire tous les `println` et appels API de notre logique de netting. Le netting doit devenir une fonction mathématique pure : `(Liste de Transactions) => Map des Positions`.

---

# 🧠 Quiz Rapide

1. Puis-je remplacer `System.currentTimeMillis()` par sa valeur dans le code ? (Non, RT violée).
2. Un programme peut-il être utile s'il n'a AUCUN effet de bord ? (Non, car il ne pourrait rien afficher ni écrire).
3. Pourquoi la RT facilite-t-elle le debugging ? (Parce que le résultat d'une fonction ne dépend que de ses arguments).

---

# 📝 Résumé du Jour

- La RT permet de raisonner sur le code comme sur des équations.
- Les effets de bord sont nécessaires mais doivent être isolés aux "bords" de l'application.
- Un code pur est plus facile à paralléliser, à tester et à maintenir.
- Tu commences aujourd'hui la "Révolution Fonctionnelle" de ton moteur.

**Prochaine étape** : Débusquer les effets de bord dans le TP 41 !
