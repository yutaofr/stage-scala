---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 9"
footer: "Jour 4 — Currying Avancé & Application Partielle"
---

# Currying & Application Partielle
## Configurer la logique sans se répéter

**Durée :** ~2h | **Fil Rouge :** Moteur de clearing paramétrable

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Currying** (transformer $f(x, y)$ en $f(x)(y)$).
- Maîtriser l'**Application Partielle** pour créer des fonctions spécialisées.
- Utiliser le currying pour injecter de la configuration (Seuils, Devises).
- Rendre le code du moteur plus générique et réutilisable.

---

# 1. Qu'est-ce que le Currying ?

C'est la technique consistant à transformer une fonction qui prend plusieurs arguments en une série de fonctions qui prennent chacune un seul argument.

### Version Classique
```scala
def multiply(a: Int, b: Int): Int = a * b
```

### Version "Curried"
```scala
def multiply(a: Int)(b: Int): Int = a * b

val double = multiply(2) _ // Application partielle
double(10) // 20
```

---

# 2. Pourquoi "Curryfier" ?

Le currying permet de "pré-remplir" certains arguments pour créer une nouvelle fonction prête à l'emploi.

### Application Partielle avec `_`
```scala
def log(level: String)(message: String): Unit = println(s"[$level] $message")

val errorLog = log("ERROR") _
val infoLog = log("INFO") _

errorLog("Base de données injoignable")
```

> 💡 C'est un moyen élégant d'injecter du contexte ou de la configuration.

---

# 3. Injection de Règles Métier

Nous allons utiliser le currying pour configurer nos seuils de validation.

```scala
def validateAmount(threshold: BigDecimal)(amount: BigDecimal): Boolean =
  amount < threshold

// Configuration pour ATH
val athValidator = validateAmount(1000000) _

// Configuration pour une petite banque partenaire
val smallBankValidator = validateAmount(50000) _
```

---

# 🏗️ Application : Le Calculateur de Frais

On peut imaginer une fonction de calcul de frais curried :
`computeFee(dailyRate: Double)(amount: BigDecimal)`.

On fixe le taux le matin une seule fois, et on obtient une fonction de frais utilisable tout au long du batch.

---

# 🧠 Quiz Rapide

1. Quelle est la différence entre une méthode normale et une méthode curried en termes de syntaxe de définition ?
2. Comment s'appelle le fait de ne fournir que certains arguments à une fonction ? (Application Partielle).
3. Quel caractère utilise-t-on souvent en Scala pour indiquer l'application partielle ? (`_`).

---

# 📝 Résumé du Jour

- Le currying transforme les fonctions multi-arguments en suites de fonctions.
- L'application partielle crée des fonctions spécialisées à partir de fonctions génériques.
- C'est l'outil idéal pour gérer la configuration et l'injection de dépendances légère.
- Ton code devient plus modulaire et adaptable à différents clients.

**Prochaine étape** : Configurer ton moteur via currying dans le TP 44 !
