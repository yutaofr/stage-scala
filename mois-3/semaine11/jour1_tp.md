# TP Jour 1 : Le Pattern Type Class

**Durée :** ~4h | **Fil Rouge :** Rendre le moteur multi-format

---

## Exercice 1 : La Type Class Serializable (1h30)

1. Crée un trait `ClearingSerializable[T]` avec une méthode `toText(value: T): String`.
2. Implémente une instance manuelle pour `Transaction` (au format CSV).
3. Implémente une instance manuelle pour `Bank` (au format CSV).
4. Écris une fonction `save[T](item: T, serializer: ClearingSerializable[T])` qui utilise l'instance pour imprimer le résultat.

---

## Exercice 2 : Extension de types tiers (1h)

1. Utilise ta Type Class `ClearingSerializable` pour rendre le type `java.time.LocalDateTime` sérialisable.
2. Formate la date selon le standard de ton choix.

---

## Exercice 3 : Multi-Instances (1h30)

1. Crée un deuxième serializer pour `Transaction` qui produit du JSON simulé (ex: `{"id": 1, ...}`).
2. Vérifie que tu peux choisir quel serializer utiliser sans modifier un seul octet de la `case class Transaction`.

**Livrable** : Code source définissant la Type Class et ses instances pour le domaine et pour des types standards Java.
