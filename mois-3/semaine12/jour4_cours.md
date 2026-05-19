---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 12"
footer: "Jour 4 — ScalaCheck & Property-Based Testing"
---

# Property-Based Testing
## Tester les lois, pas seulement les cas

**Durée :** ~2h | **Fil Rouge :** Preuve d'équilibre du clearing

---

# 📋 Objectifs du Jour

- Découvrir le **Property-Based Testing** (PBT).
- Utiliser la bibliothèque **ScalaCheck**.
- Passer du test "Unitaire" (1 cas) au test "Universel" (1000 cas).
- Prouver les invariants financiers d'ATH.

---

# 1. Test Classique vs PBT

### Test Classique (Exemple précis)
"Si je donne 100 et 200, la somme doit faire 300."

### PBT (Propriété universelle)
"Pour n'importe quel nombre $a$ et $b$, $a+b$ doit être égal à $b+a$ (Commutativité)."

```scala
property("Addition is commutative") = forAll { (a: Int, b: Int) =>
  a + b == b + a
}
```

---

# 2. Pourquoi pour le Clearing ?

Le clearing est un système critique. On ne peut pas tester toutes les combinaisons de transactions à la main.

### La Propriété d'Équilibre
"Quel que soit le batch de transactions généré, la somme des positions nettes finales doit TOUJOURS être 0."

ScalaCheck va générer des milliers de batchs (vides, énormes, avec des montants bizarres) pour tenter de "casser" notre moteur.

---

# 3. Les Générateurs (`Gen`)

On peut définir comment créer des données aléatoires pour notre domaine.

```scala
val genTransaction = for
  id  <- Gen.posNum[Int]
  amt <- Gen.choose(0.01, 1000000.0).map(v => Money(BigDecimal(v)))
yield Transaction(id, amt, ...)
```

---

# 4. Anti-Patterns des Générateurs

### ❌ Ne JAMAIS utiliser `Gen.filter` sur des critères rares

Si tu filtres pour une condition peu probable, ScalaCheck va "donner up" après trop de rejets :

```scala
// ❌ MAUVAIS : 99.9% des chaînes alphanumériques NE FONT PAS 24 caractères
val ibanGen = Gen.alphaNumStr.filter(_.length == 24)
// >> Gave up after 1 successful evaluation. 51 evaluations were discarded.
```

### ✅ TOUJOURS générer exactement ce dont tu as besoin

```scala
// ✅ BON : On génère directement 24 caractères alphanumériques
val ibanGen = Gen.listOfN(24, Gen.alphaNumChar).map(_.mkString)
```

> 🎯 Règle : **Génère, ne filtre pas.** Utilise `Gen.choose`, `Gen.listOfN`, `Gen.oneOf` pour construire précisément tes données au lieu de rejeter les mauvaises.

---

# 5. Intégration ScalaCheck + ScalaTest

Pour utiliser `forAll` dans tes tests ScalaTest existants, il faut :
1. La dépendance `scalacheck` **ET** `scalatestplus-scalacheck`.
2. Le mixin `ScalaCheckPropertyChecks` dans ta spec.

```scala
// build.sbt
"org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
"org.scalatestplus" %% "scalacheck-1-17" % "3.2.16.0" % Test

// Spec
class MonSpec extends AnyFlatSpec with ScalaCheckPropertyChecks
```

---

# 🏗️ Application : Le Stress-Test Mathématique

Nous allons soumettre le `PureNettingCalculator` au feu de ScalaCheck. Si une seule combinaison de transactions produit un déséquilibre, ScalaCheck nous donnera le cas minimal qui a échoué (**Shrinking**).

---

# 🧠 Quiz Rapide

1. Combien de tests ScalaCheck exécute-t-il par défaut pour une propriété ? (Généralement 100).
2. Qu'est-ce que le "Shrinking" ? (La capacité de ScalaCheck à réduire un cas d'échec complexe en un cas le plus simple possible pour faciliter le debug).
3. Peut-on tester des fonctions impures avec PBT ? (C'est possible mais beaucoup plus difficile et instable. PBT adore les fonctions pures).
4. **Nouveau** : Pourquoi `Gen.alphaNumStr.filter(_.length == 24)` est un anti-pattern ? (Presque toutes les chaînes seront rejetées, ScalaCheck va abandonner).

---

# 📝 Résumé du Jour

- On ne teste plus des exemples, on teste des vérités métier.
- ScalaCheck est le meilleur outil pour trouver les "Edge Cases" (cas limites).
- Prouver que `Somme == 0` sur 10 000 batchs aléatoires donne une confiance absolue dans le code.
- ⚠️ **Générer, ne pas filtrer** : c'est la règle la plus importante des Generators ScalaCheck.
- ⚠️ `scalatestplus-scalacheck` est nécessaire pour le bridge entre ScalaTest et ScalaCheck.
- Ton moteur de clearing est maintenant "certifié mathématiquement".

**Prochaine étape** : Écrire tes premières propriétés dans le TP 59 !
