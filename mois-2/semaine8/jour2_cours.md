---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 8"
footer: "Jour 2 — Collection Converters"
---

# Collections Converters
## Faire dialoguer les structures Scala et Java

**Durée :** ~2h | **Fil Rouge :** Adapter une bibliothèque de calcul Java

---

# 📋 Objectifs du Jour

- Comprendre la différence philosophique entre les collections Java (mutables) et Scala (immutables).
- Utiliser `scala.jdk.CollectionConverters` pour transformer les listes.
- Apprendre à passer d'une `java.util.List` à une `scala.List` proprement.
- Éviter les pièges de performance lors des conversions.

---

# 1. Le Choc des Cultures

### Java (`java.util.*`)
- Orientées mutation : On peut `add()`, `remove()` sur l'objet existant.
- Toutes les structures héritent de `Collection` ou `Map`.

### Scala (`scala.collection.*`)
- Orientées immuabilité : Chaque modification crée une nouvelle référence.
- Typage plus riche (Option, etc.).

---

# 2. La Solution : `asScala` et `asJava`

Depuis Scala 2.13, l'interopérabilité est centralisée.

```scala
import scala.jdk.CollectionConverters._

// 1. Recevoir du Java
val javaList: java.util.List[String] = getFromJava()
val scalaList: List[String] = javaList.asScala.toList

// 2. Envoyer vers du Java
val scalaData = List(1, 2, 3)
val javaData: java.util.List[Int] = scalaData.asJava
```

- `.asScala` : Crée un wrapper (vue) autour de la collection Java.
- `.toList` : Convertit réellement en liste immutable Scala.

---

# 3. Pièges de Performance

### Attention aux conversions répétées
Chaque `.toList` ou `.toVector` parcourt l'intégralité de la collection.

> [!TIP]
> Ne convertissez qu'aux **frontières** de votre système.
> - Entrée (Interface avec Java) : Conversion Java -> Scala.
> - Cœur du moteur : 100% Scala.
> - Sortie (Interface avec Java) : Conversion Scala -> Java.

---

# 🏗️ Application : La Lib de Taux Java

Imaginez une vieille bibliothèque `LegacyTaux.java` qui retourne un `HashMap`.

```scala
val javaRates = LegacyTaux.getRates() // java.util.HashMap
val scalaRates = javaRates.asScala.toMap // Map immutable Scala ✅
```

---

# 🧠 Quiz Rapide

1. Quel import est nécessaire pour utiliser `asScala` ?
2. Est-ce que `.asScala` crée une copie profonde de la liste ? (Non, c'est un wrapper léger).
3. Pourquoi faut-il souvent ajouter `.toList` après `.asScala` ? (Pour obtenir une structure immutable standard de Scala).

---

# ⚠️ Compromis du Stage

> [!IMPORTANT]
> **Mémoire & Conversion :** Dans notre TP, le registre Java contient quelques banques. En production, un registre peut contenir des millions d'entrées. Faire `.asScala.toList` sur une collection de 100 Go n'est pas viable. La solution industrielle est d'utiliser une **base de données** (requêtes paginées) ou du **streaming** (Akka Streams, fs2). On verra la couche base de données au Mois 3.

---

# 📝 Résumé du Jour

- On peut mélanger les collections dans le même projet.
- `asScala` et `asJava` sont les ponts indispensables.
- Restez en 100% Scala dans votre logique métier.
- L'immuabilité Scala protège vos données même si la source était mutable.

**Prochaine étape** : Adapter le service de taux dans le TP 37 !
