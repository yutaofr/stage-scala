---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 6"
footer: "Jour 1 — Sealed Traits imbriqués"
---

# Sealed Traits imbriqués
## Organiser les hiérarchies d'erreurs et de modèles

**Durée :** ~2h | **Fil Rouge :** Une hiérarchie d'erreurs exhaustive pour le clearing

---

# 📋 Objectifs du Jour

- Comprendre l'intérêt d'imbriquer des `sealed trait`.
- Créer des catégories d'erreurs (Validation, Business, Système).
- Maîtriser le pattern matching sur plusieurs niveaux.
- Gérer l'exhaustivité sur des arbres de types.

---

# 1. Pourquoi imbriquer ?

Un seul trait pour toutes les erreurs devient vite illisible (Flat hierarchy). L'imbrication permet de grouper par thématique.

```scala
sealed trait ClearingError

sealed trait ValidationError extends ClearingError
case class InvalidIban(iban: String) extends ValidationError
case class InvalidAmount(amount: BigDecimal) extends ValidationError

sealed trait BusinessError extends ClearingError
case class InsufficientBalance(iban: String) extends BusinessError
```

---

# 2. Pattern Matching par Catégorie

On peut matcher sur un trait parent pour capturer toute une catégorie.

```scala
error match
  case v: ValidationError => s"Erreur de saisie : $v"
  case b: BusinessError   => s"Règle métier non respectée : $b"
  case _                  => "Autre erreur"
```

> 💡 Cela permet de traiter les erreurs de manière générique ou spécifique selon le besoin.

---

# 3. Exhaustivité Profonde

Le compilateur vérifie l'exhaustivité à tous les niveaux.

```scala
def log(e: ClearingError) = e match
  case v: ValidationError => v match
    case InvalidIban(_)   => ...
    case InvalidAmount(_) => ...
  case b: BusinessError => ...
```

> [!IMPORTANT]
> Si vous ajoutez un nouveau type de `BusinessError`, le compilateur vous forcera à le gérer là où vous matchez sur `BusinessError`.

---

# 🏗️ Application : La "Super-Erreur"

Nous allons structurer le moteur pour qu'il distingue :
- **Echecs Bloquants** (Fichier corrompu).
- **Anomalies de Lignes** (Virement invalide).
- **Avertissements** (Banque peu connue).

---

# 🧠 Quiz Rapide

1. Peut-on hériter d'un `sealed trait` défini dans un autre fichier ? (Non).
2. Quel est l'avantage de faire un match sur un trait parent plutôt que sur chaque case class ?
3. Le mot-clé `sealed` s'applique-t-il aussi aux traits enfants ? (Oui, s'ils sont eux-mêmes sealed).

---

# 📝 Résumé du Jour

- L'imbrication des traits organise la complexité.
- Elle permet une gestion des erreurs par "couches".
- L'exhaustivité du compilateur reste ton filet de sécurité.
- Ton code devient une représentation fidèle des règles métier d'ATH.

**Prochaine étape** : Construire la hiérarchie `ClearingError` dans le TP 26 !
