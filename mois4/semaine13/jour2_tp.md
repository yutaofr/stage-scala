# TP Jour 2 : L'Essor de l'Asynchronisme

**Durée :** ~4h | **Fil Rouge :** Parallélisation massive du clearing

---

## Exercice 1 : Ma première Future (1h)

1. Crée une fonction `slowComputation(name: String): Future[String]` qui dort 2 secondes avant de renvoyer "Result: name".
2. Lance 5 appels à cette fonction en même temps.
3. Mesure le temps total d'exécution. Si c'est 2 secondes, tu as réussi. Si c'est 10 secondes, tu as codé en mode séquentiel.

---

## Exercice 2 : Validation Asynchrone (1h30)

1. Modifie ton `TransactionValidator`.
2. Sa méthode `validate` doit maintenant retourner `Future[Either[ClearingError, Transaction]]`.
3. Lance la validation d'une liste de 100 transactions fictives.
4. Utilise `Future.sequence` pour transformer ta `List[Future[...]]` en une seule `Future[List[...]]`.

---

## Exercice 3 : Agrégation Multi-Sources (1h30)

1. Simule deux services externes : `BankService` (vérifie le compte) et `AntiFraudService` (vérifie le risque).
2. Pour chaque transaction, appelle les deux services en parallèle via des Futures.
3. Combine les résultats pour n'accepter la transaction que si les deux services renvoient `Right`.

> [!TIP]
> Réutilise les types `Transaction` et `ClearingError` définis dans le package `clearing` du Mois 3. Le Kit 13.2 du starter kit montre comment les intégrer avec les Futures.

**Livrable** : Code source asynchrone utilisant les Futures et `Future.sequence` pour traiter des batchs en parallèle, avec agrégation multi-sources.
