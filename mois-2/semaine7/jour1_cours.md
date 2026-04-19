---
marp: true
theme: default
paginate: true
header: "Stage ATH — Mois 2, Semaine 7"
footer: "Jour 1 — GroupBy & MapValues sur ADTs"
---

# GroupBy & MapValues Avancés
## Organiser les flux de clearing multilatéraux

**Durée :** ~2h | **Fil Rouge :** Netting par paire de banques

---

# 📋 Objectifs du Jour

- Maîtriser l'indexation de structures complexes (`case class`).
- Utiliser `groupBy` avec des critères calculés.
- Transformer les groupes avec `mapValues` sans perdre la structure.
- Préparer les données pour le netting multilatéral.

---

# 1. GroupBy sur Propriétés d'Objets

Au lieu de simples chaînes, nous groupons maintenant des objets `Transaction`.

```scala
val txs: List[Transaction] = ...

// Grouper par banque émettrice
val parEmetteur = txs.groupBy(_.sender)
```

### Le type obtenu
`Map[String, List[Transaction]]`
- La clé est le nom de la banque.
- La valeur est la liste de tous ses envois.

---

# 2. GroupBy par Paire (Netting Bilatéral)

Pour la compensation, on veut savoir ce que la Banque A doit à la Banque B.

```scala
// Création d'une clé de groupe composée (Tuple)
val parPaire = txs.groupBy(tx => (tx.sender, tx.receiver))

// Map[(String, String), List[Transaction]]
// "ATH" -> "CIH" : List(tx1, tx4, ...)
```

---

# 3. Transformer les Groupes (`mapValues`)

Une fois groupé, on veut souvent réduire la liste à une seule valeur (ex: la somme).

```scala
val totauxParPaire = parPaire.mapValues(list => 
  list.map(_.amount).sum
)

// Map[(String, String), BigDecimal]
// ("ATH", "CIH") -> 15000.00
```

> [!NOTE]
> `mapValues` est "lazy" en Scala 2.13+, utilisez `.view.mapValues(...)` ou `.map { case (k, v) => k -> f(v) }` pour la clarté.

---

# 🏗️ Application : Tableau des Dettes

Nous allons construire la matrice des échanges interbancaires du jour.

```scala
val matrice = allTxs
  .filter(_.status == TransactionStatus.Validated)
  .groupBy(t => (t.sender, t.receiver))
  .mapValues(_.map(_.amount).sum)
```

---

# 🧠 Quiz Rapide

1. Quel est le type de retour d'un `groupBy(f)` ?
2. Peut-on grouper par un objet complet (ex: `groupBy(_.bank)`) ? (Oui, si la case class Bank est bien définie).
3. Pourquoi filtrer les statuts AVANT de grouper ? (Pour performance et justesse du calcul).

---

# 📝 Résumé du Jour

- `groupBy` est la fondation de tout calcul financier groupé.
- Les clés de groupe peuvent être des Tuples `(A, B)`.
- `mapValues` permet de passer de la liste brute au montant agrégé.
- Tu as maintenant les outils pour voir les flux entre banques.

**Prochaine étape** : Calculer le netting bilatéral dans le TP 31 !
