---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 1"
footer: "Jour 2 — Variables, Types & Précision Numérique"
---

# Variables, Types & Précision Numérique
## Les fondations d'une transaction bancaire

**Durée :** ~2h | **Fil Rouge :** Modélisation d'un virement

---

# 📋 Objectifs du Jour

- Comprendre la hiérarchie des types Scala.
- Maîtriser les types primitifs et `BigDecimal`.
- Appliquer l'immuabilité avec `val`.
- Utiliser l'inférence de type et l'interpolation.

---

# 1. Le Système de Types Scala

### Une hiérarchie unifiée
- **Any** : Le parent de tous les types.
- **AnyVal** : Types "valeurs" (Int, Double, Boolean).
- **AnyRef** : Types "références" (String, List, etc. — comme Object en Java).

> 💡 Tout est objet en Scala, même un entier !

---

# 📊 Types Primitifs & Usage Bancaire

| Type | Usage | Exemple |
|---|---|---|
| `Int` | ID de Transaction | `val id = 101` |
| `Double` | Taux d'intérêt | `val rate = 0.05` |
| `BigDecimal` | **Montants monétaires** | `val amount = BigDecimal("100.50")` |
| `String` | Libellé / IBAN | `val ref = "VIREMENT"` |
| `Boolean` | Statut de validation | `val isOk = true` |

---

# ⚠️ Le Danger du type `Double`

```scala
// Arithmétique flottante approximative
val a: Double = 0.1 + 0.2
// Résultat : 0.30000000000000004 ❌
```

### Pourquoi c'est grave ?
Dans un système de compensation, une erreur de 0.00000000000000004 cumulée sur des millions de transactions = des pertes réelles !

---

# ✅ La Solution : `BigDecimal`

```scala
// Précision décimale exacte
val b = BigDecimal("0.1") + BigDecimal("0.2")
// Résultat : 0.3 ✅
```

> [!IMPORTANT]
> Chez ATH, **tous** les calculs de montants de compensation doivent impérativement utiliser `BigDecimal`.

---

# 2. Déclaration & Immuabilité

### `val` — La Constante (Recommandé)
```scala
val montant = BigDecimal("1500.00")
// montant = BigDecimal("2000.00") // ERREUR ❌
```

### `var` — La Variable (À éviter)
```scala
var solde = 0
solde = solde + 100 // Autorisé ⚠️
```

---

# 🧠 Pourquoi l'Immuabilité ?

1. **Simplicité** : Une valeur ne change pas dans ton dos.
2. **Concurrence** : Aucun risque de "race condition" entre deux threads.
3. **Tests** : Facile à tester car prévisible.

> 💡 En programmation fonctionnelle, on privilégie toujours l'immuabilité.

---

# 3. Inférence de Type

Scala devine le type pour toi, mais tu peux le forcer.

```scala
val id = 42                // Le compilateur déduit Int
val banque = "ATH"         // Le compilateur déduit String

// On peut aussi être explicite (souvent pour la clarté) :
val montant: BigDecimal = BigDecimal("500.0")
```

---

# 4. Interpolation de Chaînes

Le préfixe `s` permet d'insérer des variables.

```scala
val bank = "ATH"
val amount = 5000

// Interpolation simple
println(s"Virement de $amount DH vers $bank")

// Avec expressions
println(s"Total : ${amount + 10} DH")

// Avec formatage (préfixe f)
println(f"Montant : $amount%2.2f DH")
```

---

# 🧠 Quiz Rapide

1. Quel type utiliser pour un montant bancaire ?
2. Quelle est la différence entre `val` et `var` ?
3. À quoi sert le `s` devant une chaîne de caractères ?

---

# 📝 Résumé du Jour

- La hiérarchie descend de `Any`.
- `BigDecimal` est crucial pour la précision financière.
- L'immuabilité (`val`) est notre réglage par défaut.
- L'interpolation rend le code plus lisible.

**Prochaine étape** : Manipuler ces types dans le TP 2 !
