---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 11"
footer: "Jour 4 — Opaque Types (Types Opaques)"
---

# Opaque Types
## La sécurité du typage fort avec 0 surcoût

**Durée :** ~2h | **Fil Rouge :** Sécuriser les IBANs et les Devises

---

# 📋 Objectifs du Jour

- Comprendre le problème de performance des "Value Classes" / `case class` simples.
- Découvrir les **Opaque Types** de Scala 3.
- Apprendre à créer des domaines "zéro-bug" sans perdre en rapidité.
- Bannir définitivement l'usage de simples `String` pour des concepts métier.

---

# 1. Le Problème de Primitive Obsession

Si je stocke tout dans des `String` :
`def process(iban: String, bankName: String)`
Rien ne m'empêche d'inverser les deux arguments par erreur ! Le compilateur ne verra rien.

### Solution v1 : Case Class
`case class Iban(value: String)`
- Sécurisé ✅.
- **Lent** ❌ : Création d'un objet en mémoire à chaque fois (Allocations).

---

# 2. La Solution Scala 3 : Opaque Type

Un type opaque est vu comme un type différent par le compilateur, mais comme un type primitif (ex: `String`) à l'exécution.

```scala
object DomainTypes:
  opaque type Iban = String
  
  object Iban:
    def apply(s: String): Iban = s // Ici on peut valider
```

- Pour le développeur : `Iban` != `String`.
- Pour la JVM : `Iban` IS `String` (Pas d'allocation d'objet !). ✅

---

# 3. Extensions & Méthodes

Les méthodes d'un type opaque sont définies via des `extension methods` **à l'intérieur** de l'objet englobant.

```scala
object DomainTypes:
  opaque type Iban = String
  extension (i: Iban)
    def country: String = i.take(2)
    def value: String = i  // Extraction de la valeur brute
```

> [!IMPORTANT]
> Seul le code à l'intérieur de `DomainTypes` a le droit de "voir" qu'un Iban est un String. Pour le reste de l'application, c'est un type scellé.

---

# 4. 🚨 Piège JVM : Type Erasure & `@targetName`

### Le problème
Quand tu définis des extensions sur **plusieurs opaques basés sur le même type** (ex: `BankCode = String` et `Iban = String`), la JVM ne voit que deux méthodes `value(String)`. C'est un conflit !

```scala
// ❌ NE COMPILE PAS sur la JVM
extension (b: BankCode) def value: String = b
extension (i: Iban)     def value: String = i
```

### La solution : `@targetName`
On donne un nom JVM distinct à chaque méthode, tout en gardant le même nom Scala.

```scala
import scala.annotation.targetName

@targetName("bankCodeValue")
extension (b: BankCode) def value: String = b

@targetName("ibanValue")
extension (i: Iban) def value: String = i
```

> [!CAUTION]
> Sans `@targetName`, tu obtiendras une erreur cryptique du genre "double definition". Retiens cette annotation !

---

# 5. 🧮 Opaque Types Numériques : `Numeric[T]` & `Ordering[T]`

### Le problème
Quand tu définis `opaque type Money = BigDecimal`, tu perds **toutes** les opérations mathématiques de BigDecimal (car Money != BigDecimal pour le compilateur).

### La solution : fournir des instances
Pour que le code existant (surtout les collections avec `.sum`, `.sorted`, `.max`) continue à fonctionner, tu dois fournir des instances de `Numeric[Money]` et `Ordering[Money]`.

```scala
object DomainTypes:
  opaque type Money = BigDecimal

  // Extensions basiques
  extension (m: Money)
    def +(other: Money): Money = m + other
    def -(other: Money): Money = m - other
    def value: BigDecimal = m

  // Instance Numeric (pour .sum, .max, etc.)
  given Numeric[Money] with
    def plus(x: Money, y: Money): Money = x + y
    def minus(x: Money, y: Money): Money = x - y
    def times(x: Money, y: Money): Money = x * y
    def negate(x: Money): Money = -x
    def fromInt(x: Int): Money = BigDecimal(x)
    def toInt(x: Money): Int = x.toInt
    def toLong(x: Money): Long = x.toLong
    def toFloat(x: Money): Float = x.toFloat
    def toDouble(x: Money): Double = x.toDouble
    def compare(x: Money, y: Money): Int = x.compare(y)
    def parseString(str: String): Option[Money] = 
      BigDecimal.exact(str) match
        case bd => Some(bd)
```

> [!TIP]
> Sans `given Numeric[Money]`, un simple `List(Money(10), Money(20)).sum` refusera de compiler. 
> C'est logique ! Le compilateur ne sait pas comment "additionner" un type qu'il ne connaît pas.

---

# 6. 📐 Stratégie de Migration Incrémentale

### ❌ Le piège du Big Bang Refactoring
Remplacer **tous** les `String` par `BankCode` d'un coup dans tout le projet va provoquer des centaines d'erreurs de compilation. C'est démoralisant et risqué.

### ✅ La bonne approche : fichier par fichier

**Étape 1** : Définir les types opaques dans `DomainTypes.scala`
**Étape 2** : Migrer le **modèle** (`Transaction`, `Bank`) en premier
**Étape 3** : Migrer les fichiers **un par un** en partant du centre (Domain) vers les bords (App)
**Étape 4** : À chaque fichier migré, compiler et vérifier

### Ordre recommandé pour le Clearing Engine :
1. `DomainTypes.scala` (Création)
2. `Transaction.scala`, `Bank.scala` (Modèle)
3. `Serializers.scala` (Adaptation des `.value`)
4. `Extensions.scala` (Adaptation du DSL)
5. `Validator.scala`, `Parser.scala` (Logique métier)
6. `ClearingApp.scala` (Point d'entrée — en dernier)

> [!WARNING]
> Ne migre **jamais** l'application principale (`*App.scala`) avant d'avoir migré toutes ses dépendances. Sinon, tu auras un effet domino incontrôlable.

---

# 🧠 Quiz Rapide

1. Un `Opaque Type` consomme-t-il plus de mémoire qu'un `String` brut ? (Non, c'est identique).
2. Peut-on additionner deux `opaque type Money = BigDecimal` directement ? (Seulement si on définit l'extension `+`).
3. Pourquoi a-t-on besoin de `@targetName` ? (Pour éviter les conflits JVM entre opaques basés sur le même type primitif).
4. Que faut-il fournir pour faire `list.sum` sur une `List[Money]` ? (Un `given Numeric[Money]`).

---

# 📝 Résumé du Jour

| Concept | Rôle | Piège à éviter |
| :--- | :--- | :--- |
| `opaque type` | Sécurité sans overhead | Penser que les opérateurs marchent "automatiquement" |
| `@targetName` | Résoudre les conflits JVM | Oublier l'annotation sur les extensions similaires |
| `Numeric[T]` | Activer `.sum`, `.max`, etc. | Ne pas le fournir et bloquer toutes les collections |
| Migration | Fichier par fichier, du centre vers les bords | Faire un "Big Bang" qui casse tout |

**Prochaine étape** : Appliquer cette stratégie de migration dans le TP 54 !
