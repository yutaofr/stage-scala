---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 4"
footer: "Jour 3 — Option + Pattern Matching"
---

# Option & Pattern Matching
## La manière élégante de déballer les valeurs

**Durée :** ~2h | **Fil Rouge :** Construction d'un parser CSV résilient v0.4

---

# 📋 Objectifs du Jour

- Utiliser le **Pattern Matching** sur les `Option`.
- Maîtriser les **For-yield** sur les `Option` (composition propre).
- Découvrir le pattern "Safe Get" sur les Maps.
- Rendre le parser de transactions indestructible.

---

# 1. Matching sur `Some` et `None`

Le pattern matching est le moyen le plus explicite de gérer une option.

```scala
val maybeAmount: Option[BigDecimal] = ...

maybeAmount match
  case Some(amt) => println(s"Montant : $amt DH")
  case None      => println("Aucun montant fourni")
```

> 💡 C'est visuel, sécurisé et le compilateur vérifie l'exhaustivité.

---

# 2. Composition avec For-Yield

Le `flatMap` peut vite devenir illisible (callback hell). Le `for-yield` simplifie tout.

```scala
// Si m1 existe ET m2 existe -> calcule la somme
// Sinon -> None
val total = for
  m1 <- findAmount("001")
  m2 <- findAmount("002")
yield m1 + m2
```

> [!IMPORTANT]
> Si une seule des étapes renvoie `None`, tout le bloc renvoie `None` immédiatement. C'est le comportement "fail-fast".

---

# 3. Le pattern "Safe Get"

En Scala, appeler `map(key)` crashe si la clé n'existe pas. On utilise `.get(key)`.

```scala
val rates = Map("EUR" -> 10.85)

// ❌ Risque de crash
val r1 = rates("USD") 

// ✅ Sécurisé
val r2 = rates.get("USD") // Option[Double] = None
```

---

# 🏗️ Application : Parser Résilient

Au lieu de crasher sur une mauvaise ligne, le parser renvoie `Option[Transaction]`.

```scala
def parse(line: String): Option[Transaction] =
  line.split(",") match
    case Array(f, t, a) if isNumeric(a) => 
      Some(Transaction(f, t, BigDecimal(a)))
    case _ => 
      None
```

---

# 🧠 Quiz Rapide

1. Que se passe-t-il dans un `for-yield` sur Options si la première valeur est `None` ?
2. Quelle méthode de `Map` retourne une `Option` au lieu de crasher ?
3. Le Pattern Matching sur Option est-il exhaustif sans `case _` ? (Oui, car il n'y a que 2 sous-types).

---

# 📝 Résumé du Jour

- Le Pattern Matching est l'outil de lecture préféré pour les Options.
- `for-yield` permet d'enchaîner plusieurs opérations optionnelles sans imbrication.
- Le style "total" (fonctions qui retournent toujours une valeur, même `None`) évite les bugs de production.

**Prochaine étape** : Créer un parser indestructible dans le TP 17 !
