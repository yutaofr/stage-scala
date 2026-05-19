---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 3, Semaine 11"
footer: "Jour 1 — Introduction aux Type Classes"
---

# Type Classes
## Le polymorphisme sans héritage

**Durée :** ~2h | **Fil Rouge :** Sérialisation extensible du Clearing Engine

---

# 📋 Objectifs du Jour

- Comprendre pourquoi l'héritage classique est parfois rigide.
- Découvrir le pattern **Type Class**.
- Apprendre à séparer la donnée du comportement.
- Créer une Type Class `Serializable[T]`.

---

# 1. Le Problème de l'Héritage

Si je veux rendre ma `Transaction` sérialisable en JSON, je pourrais faire :
`case class Transaction(...) extends JsonSerializable`.

### Problèmes
- Je pollue mon modèle métier avec des détails techniques.
- Si je veux ajouter du CSV plus tard, je dois encore modifier la classe.
- Je ne peux pas ajouter ce comportement à des classes que je ne possède pas (ex: `String`, `Int`).

---

# 2. La Solution : Type Class

Une Type Class est un trait générique qui définit un comportement.

```scala
trait Serializer[T]:
  def serialize(value: T): String
```

On peut alors créer des instances pour nos types :
```scala
object TransactionSerializer extends Serializer[Transaction]:
  def serialize(tx: Transaction): String = s"ID:${tx.id},AMT:${tx.amount}"
```

---

# 3. Pourquoi c'est "Magique" ?

- **Découplage** : La `Transaction` ne sait même pas qu'elle est sérialisable.
- **Extensibilité** : On peut ajouter la sérialisation XML demain sans toucher au domaine.
- **Ad-hoc** : On peut rendre n'importe quel type sérialisable après coup.

---

# 🏗️ Application : Multi-Format

Nous allons définir une Type Class pour la sérialisation, et implémenter des instances pour CSV et JSON (via des mocks).

---

# 🧠 Quiz Rapide

1. Une Type Class nécessite-t-elle l'usage de `extends` sur la classe de donnée ? (Non).
2. Où définit-on généralement le trait de la Type Class ? (Dans un fichier séparé du domaine).
3. Quel est l'avantage principal par rapport à l'héritage ? (Le découplage et l'extensibilité ad-hoc).

---

# 📝 Résumé du Jour

- Les Type Classes sont le "Design Pattern" le plus puissant de la FP.
- Elles permettent d'ajouter des comportements de manière externe et modulaire.
- C'est la base de bibliothèques célèbres comme **Cats** ou **ZIO**.
- Ton moteur devient une plateforme extensible.

**Prochaine étape** : Créer tes premières Type Classes dans le TP 51 !
