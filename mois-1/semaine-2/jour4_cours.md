---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 1, Semaine 2"
footer: "Jour 4 — Traits & Interfaces"
---

# Traits & Interfaces
## Le contrat de collaboration en Scala

**Durée :** ~2h | **Fil Rouge :** Définir le contrat du moteur de clearing

---

# 📋 Objectifs du Jour

- Comprendre le concept de **Trait** (l'interface puissance 10).
- Apprendre à définir des méthodes abstraites et concrètes.
- Maîtriser le **mix-in** (composition de comportements).
- Structurer le code par des contrats clairs.

---

# 1. Qu'est-ce qu'un Trait ?

Un `trait` définit un ensemble de membres (méthodes, variables) qu'une classe ou un objet peut implémenter.

```scala
trait Validateur:
  def estValide(montant: BigDecimal): Boolean // Abstraite
```

### Pourquoi "Interface puissance 10" ?
Contrairement aux interfaces Java classiques, un trait peut contenir du code (méthodes implémentées) et même des variables.

---

# 🏗️ Implémentation d'un Trait

On utilise le mot-clé `extends`.

```scala
object ValidateurBancaire extends Validateur:
  def estValide(montant: BigDecimal): Boolean = 
    montant > 0 && montant < 1000000
```

> 💡 On dit que `ValidateurBancaire` respecte le **contrat** de `Validateur`.

---

# 🧩 Composition (Mix-in)

Un des grands atouts des traits est qu'on peut en combiner plusieurs avec `with`.

```scala
trait Logger:
  def log(msg: String): Unit = println(s"[INFO] $msg")

object SuperProcessor extends Validateur with Logger:
  def process(m: BigDecimal) =
    if estValide(m) then log("Traitement OK")
```

---

# 🏛️ Exemple : ClearingService

Définir l'ossature de notre moteur.

```scala
trait ClearingService:
  def ingest(file: String): List[Transaction]
  def process(txs: List[Transaction]): List[Result]
  
  // Méthode concrète partagée
  def report(res: List[Result]): Unit = 
    println(s"Génération du rapport pour ${res.size} résultats")
```

---

# 🧠 Quiz Rapide

1. Quelle est la différence majeure entre un `trait` Scala et une `interface` Java 7 ?
2. Quel mot-clé utilise-t-on pour ajouter un deuxième trait à un objet ?
3. Un trait peut-il contenir des méthodes déjà implémentées ?

---

# 📝 Résumé du Jour

- Un `trait` définit un contrat (le "quoi").
- Il peut fournir une implémentation par défaut (le "comment" partagé).
- On peut "mixer" plusieurs traits pour composer des fonctionnalités complexes.
- C'est l'outil principal pour l'architecture modulaire en Scala.

**Prochaine étape** : Construire l'architecture du moteur dans le TP 9 !
