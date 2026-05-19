# TP Jour 2 : Abstraction Contextuelle

**Durée :** ~4h | **Fil Rouge :** Automatiser la sérialisation du clearing

---

## Exercice 1 : Migration vers using/given (1h30)

1. Reprends ta Type Class `ClearingSerializable` du Jour 1.
2. Définis l'instance pour `Transaction` comme un `given`.
3. Réécris ta fonction `save` pour qu'elle accepte un paramètre `using`.
4. Appelle `save(myTx)` et vérifie que ça compile et s'exécute sans passer l'instance manuellement.

---

## Exercice 2 : Local vs Global (1h)

1. Crée un deuxième `given` pour `Transaction` (ex: format XML) dans un objet séparé.
2. Importe-le localement dans un test.
3. Observe comment le compilateur choisit l'instance importée plutôt que l'instance par défaut.

---

## Exercice 3 : Le Multi-Exporter (1h30)

1. Crée une fonction `exportBatch[T](items: List[T])(using serializer: ClearingSerializable[T]): String`.
2. Elle doit concaténer les versions sérialisées de tous les items.
3. Utilise-la pour exporter à la fois une liste de `Transaction` et une liste de `Bank`.

**Livrable** : Code source utilisant `given` et `using` pour supporter la sérialisation automatique de différents types de domaine.
