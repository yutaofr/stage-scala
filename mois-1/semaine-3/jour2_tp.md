# TP Jour 2 : Pipeline de Transformation

**Durée :** ~4h | **Fil Rouge :** Nettoyer et enrichir le flux de transactions brutes

---

## Exercice 1 : Enrichissement de Données (1h30)

1. Tu as une liste de transactions brutes : `List(("001", "002", 1500), ("002", "001", 300))`.
2. Utilise `map` pour transformer cette liste en une liste de tuples enrichis contenant les noms complets des banques (utilise la `repertoire` Map de l'exercice précédent).
3. Résultat attendu : `List(("Attijariwafa Bank", "CIH Bank", 1500), ...)`

---

## Exercice 2 : Filtrage Sécurisé (1h)

1. À partir de la liste enrichie, filtre les transactions dont le montant est supérieur à 5000 DH (audit requis).
2. Filtre également les transactions où la banque source est la même que la banque destination (virements internes, ignorés par le clearing interbancaire).

---

## Exercice 3 : Éclatement de Lots (1h30)

1. Parfois, une banque envoie un "Lot" de transactions : `case class Lot(info: String, amounts: List[BigDecimal])`.
2. Tu as une `List[Lot]`.
3. Utilise `flatMap` pour obtenir une liste unique et plate de tous les montants contenus dans tous les lots.
4. Applique ensuite un `filter` pour ne garder que les montants > 0.

---

## Exercice 4 : Comparaison For vs Pipeline (30 min)

1. Réécris l'exercice 1 en utilisant une `for-comprehension`.
2. Laquelle des deux écritures trouves-tu la plus lisible ? Pourquoi ?

**Livrable** : Code montrant un pipeline complet : `list.filter(...).map(...).flatMap(...)`.
