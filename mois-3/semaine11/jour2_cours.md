---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 11"
footer: "Jour 2 — Context Parameters (using / given)"
---

# Context Parameters
## L'injection de dépendances élégante de Scala 3

**Durée :** ~2h | **Fil Rouge :** Injection implicite du format de sortie

---

# 📋 Objectifs du Jour

- Découvrir les mots-clés `given` et `using`.
- Automatiser la sélection des instances de Type Class.
- Comprendre la résolution implicite de Scala 3.
- Simplifier l'appel des fonctions génériques.

---

# 1. Le Problème du Passage Manuel

Hier, nous devions passer manuellement le serializer :
`save(tx, txSerializer)`.

### Le rêve
Avoir une fonction qui dit : "Je sais sauvegarder n'importe quoi, pourvu qu'un serializer pour ce type existe quelque part dans le contexte."

---

# 2. `given` & `using`

### Définir une instance disponible (`given`)
```scala
given txSerializer: ClearingSerializable[Transaction] with
  def toText(tx: Transaction): String = ...
```

### Utiliser une instance automatiquement (`using`)
```scala
def save[T](item: T)(using serializer: ClearingSerializable[T]): Unit =
  println(serializer.toText(item))

// Appel
save(tx) // Scala trouve tout seul le 'given' correspondant ! ✅
```

---

# 3. La Résolution Implicite

Le compilateur cherche une instance de `ClearingSerializable[T]` dans :
1. Le scope courant (le fichier).
2. Les imports.
3. Le compagnon du type `T` (endroit recommandé).

> [!TIP]
> Si plusieurs instances correspondent, le compilateur affiche une erreur pour ambiguïté. C'est prévisible et sûr.

---

# 🏗️ Application : Le ExportEngine

Nous allons créer un moteur d'export qui prend une liste d'objets et les exporte dans le format "donné" par le contexte (CSV ou JSON).

---

# 🧠 Quiz Rapide

1. Quel mot-clé remplace `implicit val` en Scala 3 ? (`given`).
2. Quel mot-clé remplace l'ancien paramètre `implicit` dans une fonction ? (`using`).
3. Est-il possible d'avoir un `given` dans un autre fichier ? (Oui, via un import).

---

# 📝 Résumé du Jour

- `given` et `using` implémentent le pattern Type Class de manière fluide.
- Le compilateur fait le travail pénible de passage des arguments techniques.
- Ton code métier reste "propre" (il ne voit plus les serializers).
- C'est l'essence du style "Contextual Abstraction" de Scala 3.

**Prochaine étape** : Automatiser tes exports dans le TP 52 !
