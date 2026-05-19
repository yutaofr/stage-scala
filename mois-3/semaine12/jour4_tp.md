# TP Jour 4 : Certification par les Propriétés

**Durée :** ~4h | **Fil Rouge :** Garantir l'équilibre du système interbancaire

---

## Exercice 0 : Configuration (15 min)

Avant de commencer, assure-toi que `build.sbt` contient les **deux** dépendances :

```scala
"org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
"org.scalatestplus" %% "scalacheck-1-17" % "3.2.16.0" % Test
```

> ⚠️ Sans `scalatestplus`, le `forAll` de ScalaCheck ne sera pas trouvé dans tes tests ScalaTest.

---

## Exercice 1 : Ma première propriété (1h)

1. Crée une nouvelle spec `PropertySpec` qui étend `AnyFlatSpec with Matchers with ScalaCheckPropertyChecks`.
2. Définis une propriété simple : un identifiant IBAN simulé a toujours 24 caractères.
3. Lance le test sur 100 chaînes générées.

> ⚠️ **Piège classique** : Utilise `Gen.listOfN(24, Gen.alphaNumChar).map(_.mkString)` plutôt que `Gen.alphaNumStr.filter(_.length == 24)`. Le `.filter` rejette trop de candidats et ScalaCheck abandonne ("Gave up after N discarded").

---

## Exercice 2 : Générateur de Domaine (1h30)

1. Crée un générateur `Gen[Transaction]` qui produit des transactions avec :
   - des montants aléatoires en `BigDecimal` entre 0.01 et 1 000 000.00.
   - des banques choisies parmi un pool fixe (`Gen.oneOf("AWB", "BMCE", "CIH")`).
2. Crée un générateur `Gen[List[Transaction]]` qui produit des batchs avec `Gen.listOfN(200, transactionGen)`.

> 💡 Utilise les Opaque Types dans le générateur : `BankCode.unsafe(...)` et `Money(BigDecimal(...))` pour que les transactions générées soient compatibles avec ton domaine typé.

---

## Exercice 3 : L'Invariant Suprême (1h30)

1. Implémente la propriété : `PureNettingCalculator.calculateNetPositions(batch).values.sum == 0`.
2. Fais tourner le test sur des centaines de batchs aléatoires.
3. Si le test échoue (à cause des arrondis par exemple), corrige ta logique de calcul pour utiliser `BigDecimal` correctement partout.

> 💡 Pense à filtrer les auto-transferts (quand `sender == receiver`) dans la propriété plutôt que dans le calculateur, car ScalaCheck peut toujours en générer.

**Livrable** : Suite de tests ScalaCheck prouvant l'équilibre financier du moteur sur des milliers de cas générés.
