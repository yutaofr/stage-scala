---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 2"
footer: "Jour 2 — Pattern Matching Avancé"
---

# Pattern Matching Avancé
## Sécuriser la logique métier par le typage

**Durée :** ~2h | **Fil Rouge :** Validation exhaustive des transactions

---

# 📋 Objectifs du Jour

- Maîtriser le **Pattern Matching** sur les Tuples.
- Apprendre le matching sur les types (Type Testing).
- Utiliser le matching imbriqué et les conditions complexes.
- Comprendre pourquoi le compilateur est un allié pour la sécurité.

---

# 1. Matching sur les Tuples

On peut extraire et tester des valeurs spécifiques à l'intérieur d'un tuple en une seule opération.

```scala
val tx = (1, "ATH", "CIH", BigDecimal("500"), "VIR")

tx match
  case (id, from, to, amount, "VIR") => 
    s"Virement #$id de $from vers $to"
  case (id, _, _, amount, "PRE") if amount > 1000 => 
    s"Gros prélèvement #$id"
  case (id, _, _, _, txType) => 
    s"Transaction #$id de type $txType"
```

---

# 🔎 Le Caractère `_` (Wildcard)

Le tiret bas `_` signifie "n'importe quelle valeur".

- On l'utilise quand on n'a pas besoin de la valeur pour la suite.
- Cela évite de polluer le code avec des variables inutiles.

> 💡 Dans `case (id, _, _, amount, "PRE")`, on ignore l'expéditeur et le destinataire.

---

# 2. Matching sur les Types

En Scala, on peut tester le type d'une donnée directement dans le match.

```scala
def decrire(valeur: Any): String = valeur match
  case i: Int        => s"C'est un entier : $i"
  case s: String     => s"C'est du texte : $s"
  case b: BigDecimal => s"C'est un montant : $b"
  case _             => "Type inconnu"
```

> ⚠️ À utiliser avec parcimonie (on préfère généralement les types plus précis).

---

# 3. Matching Imbriqué & Gardes

On peut combiner des structures et des conditions logiques complexes.

```scala
def analyser(tx: (String, BigDecimal)): String =
  tx match
    case ("VIR", amnt) if amnt > 10000 => "🚨 Audit requis (Gros virement)"
    case ("VIR", _)                    => "✅ Virement standard"
    case ("PRE", amnt) if amnt < 0     => "❌ ERREUR : Prélèvement négatif"
    case (txType, _)                   => s"Info : $txType"
```

---

# 🛡️ La Sécurité au Typage

Le compilateur vérifie si ton `match` couvre tous les cas possibles.

- **Avertissement d'exhaustivité** : Si tu oublies une branche, le compilateur te prévient (Match is not exhaustive).
- **Règle d'or** : Toujours finir par un cas par défaut `case _` si l'exhaustivité n'est pas garantie par le type.

---

# 🧠 Quiz Rapide

1. Que signifie le caractère `_` dans un pattern match ?
2. Comment ajouter une condition `if` à un `case` ?
3. Le compilateur nous aide-t-il si on oublie un cas dans un `match` ?

---

# 📝 Résumé du Jour

- Le Pattern Matching est l'outil de base pour déstructurer des tuples.
- Il permet de tester les types et les valeurs simultanément.
- Les gardes (`if`) ajoutent de la flexibilité aux branches du match.
- C'est la fondation d'un code résilient et sans bugs.

**Prochaine étape** : Implémenter des validateurs complexes dans le TP 7 !
