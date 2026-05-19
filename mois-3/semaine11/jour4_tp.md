# TP Jour 4 : Dompter les Types Opaques

**Durée :** ~4h | **Fil Rouge :** Sécuriser le domaine bancaire d'ATH

---

## Exercice 1 : Identifiants Opaques (1h)

1. Crée un fichier `DomainTypes.scala` dans le package `clearing.model`.
2. Définis `opaque type BankCode = String`.
3. Définis `opaque type Iban = String`.
4. Ajoute des factory methods :
   - `unsafe(s: String)` : crée l'opaque sans validation (pour les tests et le bootstrap).
   - `apply(s: String): Either[String, Iban]` : valide la longueur (IBAN = 24 chars) avant de créer.
5. Ajoute une extension `value` sur chaque type pour extraire la String brute.
6. **Important** : Utilise `@targetName` pour les extensions sur `BankCode` et `Iban`, car les deux sont des Strings !

```scala
import scala.annotation.targetName

@targetName("bankCodeValue")
extension (b: BankCode) def value: String = b

@targetName("ibanValue")
extension (i: Iban) def value: String = i
```

**Vérification** : Le code suivant **ne doit pas compiler** :
```scala
val code: BankCode = BankCode.unsafe("ATH")
val iban: Iban = code  // ❌ Erreur de compilation attendue !
```

---

## Exercice 2 : Argent Opaque (1h30)

1. Définis `opaque type Money = BigDecimal` dans le même `DomainTypes`.
2. Ajoute les extensions suivantes :
   - `+`, `-` : opérations arithmétiques.
   - `*` (avec un `Double`) : pour les taux de change et frais.
   - `value` : extraction de la valeur brute.
   - `format` : affiche avec le symbole "DH" (ex: `5 000.00 DH`).
   - `isPositive` : vérifie que le montant est > 0.
   - `abs` : valeur absolue.
3. **Fournis un `given Numeric[Money]`** pour activer les opérations de collection :

```scala
// Test cible : ce code doit compiler et fonctionner
val amounts = List(Money(100), Money(200), Money(300))
val total = amounts.sum        // Nécessite Numeric[Money]
val maxVal = amounts.max       // Nécessite Ordering[Money]
println(total.format)          // "600.00 DH"
```

4. Modifie ta `case class Transaction` pour utiliser `BankCode` (sender/receiver) et `Money` (amount).

---

## Exercice 3 : Migration Incrémentale du Pipeline (1h30)

> [!WARNING]
> Ne fais PAS de Big Bang ! Migre fichier par fichier en suivant cet ordre.

### Étape 1 — Modèle (15 min)
1. Ouvre `Transaction.scala` et remplace `sender: String` par `sender: BankCode`, `receiver: String` par `receiver: BankCode`, et `amount: BigDecimal` par `amount: Money`.
2. Compile. Observe la liste d'erreurs, c'est normal — on va les corriger fichier par fichier.

### Étape 2 — Serializers (15 min)
1. Ouvre `Serializers.scala`.
2. Remplace chaque accès direct (ex: `tx.sender`) par `tx.sender.value` dans les chaînes d'interpolation.
3. Compile uniquement `Serializers.scala` (ou observe la réduction du nombre d'erreurs).

### Étape 3 — Extensions (10 min)
1. Ouvre `Extensions.scala`.
2. Remplace `tx.sender.startsWith("MA")` par `tx.sender.value.startsWith("MA")`.

### Étape 4 — Validator (15 min)
1. Mets à jour les signatures pour accepter `BankCode` et `Money`.
2. Utilise `.value` là où tu accédais aux valeurs brutes.

### Étape 5 — Parser & Pipeline (20 min)
1. Adapte `ResilientParser` pour construire `BankCode.unsafe(...)` et `Money(...)` lors du parsing.
2. Adapte `ClearingPipeline` si nécessaire.

### Étape 6 — Application (15 min)
1. En dernier, adapte ton `ClearingAppV21` pour construire les bons types à partir des arguments.
2. Compile et lance les tests.

> [!TIP]
> À chaque étape, tu devrais voir le nombre d'erreurs de compilation **diminuer**. C'est le signe que tu migres dans le bon ordre.

**Livrable** : Code source du domaine utilisant exclusivement des types opaques, migré fichier par fichier sans interruption de service.
