---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 5"
footer: "Jour 4 — Composition d'ADTs"
---

# Composition d'ADTs
## Structurer la complexité sans perdre la pureté

**Durée :** ~2h | **Fil Rouge :** Modélisation d'un Batch de clearing

---

# 📋 Objectifs du Jour

- Apprendre à composer plusieurs ADTs (imbrication).
- Modéliser des enregistrements immuables complexes.
- Utiliser le Pattern Matching profond.
- Gérer les listes de données typées au sein des structures.

---

# 1. L'Imbrication d'ADTs

Un domaine métier n'est jamais plat. Les structures s'imbriquent.

```scala
case class ClearingBatch(
  id: Int,
  transactions: List[Transaction], // Liste de types Produits
  status: TransactionStatus        // Type Somme
)
```

### Pourquoi c'est puissant ?
On peut représenter un état complexe (ex: "Un lot de 500 virements en attente de validation") de manière atomique et immuable.

---

# 2. Pattern Matching Profond

On peut descendre dans la structure pour tester des valeurs précises.

```scala
batch match
  case ClearingBatch(_, txs, TransactionStatus.Pending) if txs.isEmpty =>
    "Lot vide en attente — À supprimer"
  case ClearingBatch(id, _, TransactionStatus.Rejected) =>
    s"Le lot #$id a été globalement rejeté"
```

> 💡 On peut ignorer des parties entières (`_`) ou extraire des listes complètes.

---

# 3. Records Immuables & État

En Scala, pour "modifier" un lot, on en crée un nouveau.

```scala
val batch1 = ClearingBatch(1, List(tx1), TransactionStatus.Pending)

// Évolution de l'état
val batch2 = batch1.copy(status = TransactionStatus.Validated)
```

- Pas d'effets de bord.
- Historique préservé.
- Auditabilité totale des transitions d'état.

---

# 🏗️ Exemple : Le Résultat de Compensation

Une structure qui regroupe tout le savoir à un instant T.

```scala
case class ClearingResult(
  batch: ClearingBatch,
  netPositions: Map[String, BigDecimal],
  errors: List[ClearingError]
)
```

---

# 🧠 Quiz Rapide

1. Comment s'appelle le fait de mettre une `case class` comme champ d'une autre `case class` ?
2. Peut-on faire un `match` sur une liste à l'intérieur d'une structure ?
3. Le pattern matching profond permet-il d'extraire plusieurs niveaux d'imbrication à la fois ?

---

# 📝 Résumé du Jour

- La composition d'ADTs permet de modéliser des domaines complexes.
- L'immuabilité garantit que l'état d'un lot ne change jamais par accident.
- Le pattern matching profond offre une lecture chirurgicale des données.
- Ton moteur est maintenant prêt à gérer des volumes structurés (Batches).

**Prochaine étape** : Implémenter le pipeline de Batch dans le TP 24 !
