---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 6"
footer: "Jour 4 — Companion Objects & Factory Methods avancées"
---

# Factory Avancées & Compagnons
## Création d'objets sécurisée et propre

**Durée :** ~2h | **Fil Rouge :** Construction du parser CSV v1.1

---

# 📋 Objectifs du Jour

- Approfondir le rôle du **Companion Object**.
- Créer des **Factory Methods** qui retournent des types sécurisés (`Option`, `Either`).
- Centraliser la logique de construction et de parsing.
- Préparer le passage du "texte brute" vers le "modèle typé".

---

# 1. Le Gardien du Temple

Le companion object est l'endroit idéal pour placer la logique de création. 

```scala
case class Transaction(id: Int, amount: BigDecimal)

object Transaction:
  // Factory sécurisée
  def fromCsv(line: String): Option[Transaction] =
    val parts = line.split(",")
    if parts.length == 2 then
      // Tentative de parsing du montant
      Try(Transaction(parts(0).toInt, BigDecimal(parts(1)))).toOption
    else
      None
```

> [!NOTE]
> **Compromis Métier** : Nous avons ajouté un enum `Currency` (MAD, EUR, USD) pour apprendre le concept de Factory. Cependant, notre algorithme de netting reste **mono-devise (MAD)**. En production, une conversion de taux serait nécessaire avant le calcul du solde net.


---

# 2. Pattern : Multiple Factory Methods

On peut offrir plusieurs manières de créer un objet.

```scala
object Bank:
  // Depuis des codes techniques
  def fromCodes(id: Int, swift: String): Bank = ...
  
  // Depuis un fichier de config
  def fromConfig(raw: String): Option[Bank] = ...
```

> 💡 Cela permet de garder les constructeurs de `case class` simples et de déporter la logique "sale" (parsing, calculs) dans le compagnon.

---

# 3. Encapsulation (Private Constructor)

On peut forcer l'usage de la Factory en rendant le constructeur privé.

```scala
case class SecureToken private (value: String)

object SecureToken:
  def apply(raw: String): Option[SecureToken] =
    if raw.length > 20 then Some(new SecureToken(raw)) else None
```

> 💡 Ainsi, il est **impossible** de créer un token invalide dans le reste de l'application.

---

# 🏗️ Application : Le Parser v1.1

Toute la logique de transformation CSV de la semaine 2 est maintenant centralisée dans `Transaction.fromCsv`.

```scala
val transactions = csvLines.flatMap(Transaction.fromCsv)
```

- Pas de logique éparpillée.
- Facile à tester unitairement.
- Code principal (Main) devient extrêmement court et lisible.

---

# 🧠 Quiz Rapide

1. Où doit-on placer une méthode qui crée un objet à partir d'un fichier JSON ?
2. Comment empêcher la création directe d'une `case class` via son constructeur ?
3. Pourquoi retourner une `Option` plutôt que de lancer une exception dans une Factory ?

---

# 📝 Résumé du Jour

- Les compagnons sont les usines (factories) de tes objets.
- Ils protègent l'intégrité de tes données en vérifiant les règles à la création.
- Ils permettent d'unifier la manière de transformer le monde extérieur (Strings, JSON) en objets métier.
- Ton code gagne en cohérence et en sécurité.

**Prochaine étape** : Réécrire tes factories dans le TP 29 !
