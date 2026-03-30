---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 2"
footer: "Jour 3 — Strings, Regex & Parsing"
---

# Strings, Regex & Parsing
## Nettoyage et extraction de données bancaires

**Durée :** ~2h | **Fil Rouge :** Construction d'un parser CSV résilient

---

# 📋 Objectifs du Jour

- Maîtriser les méthodes utilitaires de `String`.
- Utiliser les expressions régulières (**Regex**) en Scala.
- Extraire des données par Pattern Matching sur Regex.
- Préparer le nettoyage des fichiers de transactions.

---

# 1. Manipulation de Chaînes

Scala utilise les Strings de la Java, mais avec des méthodes plus ergonomiques.

### Méthodes essentielles
```scala
val raw = "  ATH,CIH,500.00  "

raw.trim        // "ATH,CIH,500.00"
raw.split(",")  // Array("ATH", "CIH", "500.00")
raw.contains("ATH") // true
raw.startsWith("  ") // true
```

---

# 2. Les Regex en Scala

Une Regex se définit simplement avec `.r` sur une chaîne triple-quotes (pour éviter l'échappement des `\`).

```scala
val IbanRegex = """MA\d{22}""".r

val input = "Mon IBAN est MA123456789012345678901234"

IbanRegex.findFirstIn(input) // Option[String] = Some(MA1234...)
```

---

# 🔓 Extraction par Pattern Matching

C'est la fonctionnalité la plus puissante des Regex en Scala : on définit des groupes de capture avec `()`.

```scala
val TransactionRegex = """(\w+),(\w+),(\d+\.\d+)""".r

"ATH,CIH,150.50" match
  case TransactionRegex(source, dest, montant) =>
    println(s"$source a envoyé $montant à $dest")
  case _ =>
    println("Ligne malformée ❌")
```

---

# 🏗️ Application au Parsing CSV

Le format CSV est souvent piégeux (espaces, formats de nombres).

```scala
def parseLine(line: String) = line.split(",").map(_.trim) match
  case Array(id, from, to, amount) =>
    // Conversion et validation...
  case _ =>
    // Gestion d'erreur...
```

---

# 🧠 Quiz Rapide

1. Quelle méthode permet d'enlever les espaces inutiles en début et fin de chaîne ?
2. Comment transforme-t-on une `String` en objet `Regex` ?
3. À quoi servent les parenthèses `()` dans une Regex Scala ?

---

# 📝 Résumé du Jour

- Les méthodes `String` couvrent 80% des besoins simples de nettoyage.
- Les `Regex` sont indispensables pour les formats complexes.
- Le Pattern Matching permet d'extraire des groupes de capture de manière élégante.
- Parser une ligne, c'est d'abord la nettoyer, puis la structurer.

**Prochaine étape** : Créer ton premier parser CSV dans le TP 8 !
