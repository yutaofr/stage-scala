# Marc — Semaine 5 v1.0 Design

## Intention

La version v1.0 remplace les tuples positionnels du prototype par un modèle
métier nommé. Elle rend les états, les types de transaction et les erreurs
énumérables par le compilateur. La semaine 6 introduira `Either` et
`Validated`; la semaine 5 conserve donc des `List` et des objets de résultat
nommés.

## Frontière de migration

Le paquet `clearing.model` contient les données et les ADT. Le paquet
`clearing.v10` contient le parser, le générateur, la validation, le netting et
l'application v1.0. Les modules v0.x restent intacts pour prouver la
non-régression, mais l'application v1.0 ne les appelle pas.

Cette frontière donne un sens précis au critère « aucun tuple anonyme dans la
logique métier » : aucun fichier de `clearing.model` ou `clearing.v10` ne
déclare ni ne manipule de tuple. Les prototypes v0.x restent des exemples de
l'état antérieur à la migration.

## Modèle

- `Bank`, `Account` et `Transaction` sont des case classes immuables.
- `TransactionStatus` contient `Pending`, `Validated`, `Rejected` et
  `Suspicious`; le quatrième cas ferme l'exercice d'exhaustivité du jour 3.
- `TransactionType` contient `Transfer`, `Withdrawal` et `Check`. Son
  compagnon traduit les codes CSV `VIR`, `PRE` et `CHQ`.
- `ClearingError` contient `InvalidAmount`, `UnknownBank`,
  `DuplicateTransaction` et `ValidationError`.
- `InvalidTransaction` et `ValidationSummary` remplacent le couple anonyme
  « valides / invalides » par des noms.
- `ClearingBatch`, `ClearingResult` et `AppResult` portent les résultats du lot
  et de l'application.

`Transaction.sender` et `Transaction.receiver` restent des codes bancaires
`String`, comme le demande le TP. `Bank` représente les référentiels et les
comptes. La validation relie les codes à ce référentiel et produit
`UnknownBank` lorsqu'un code manque.

## Flux v1.0

1. `CsvParserV10` transforme les lignes bien formées en `Transaction`.
2. `TransactionValidator` accumule les erreurs d'une transaction et marque
   seulement les occurrences répétées d'un identifiant comme doublons.
3. `BatchProcessor` calcule les positions à partir des seules transactions
   valides et conserve toutes les erreurs typées.
4. `ClearingAppV10` rend un rapport déterministe avec les comptes de lignes,
   les positions et les causes de rejet disponibles.

Le parser retourne encore `Option`, donc les lignes syntaxiquement illisibles
n'ont pas de cause structurée. Ce manque reste volontaire et devient le point
de départ de la semaine 6.

## Invariants

- Une transaction est de forte valeur seulement si son montant dépasse
  strictement 50 000 DH.
- Un montant doit être strictement positif.
- Les deux codes bancaires doivent exister et être distincts.
- La première occurrence d'un identifiant est candidate; les suivantes sont
  des doublons.
- Chaque transaction nette débite l'émetteur et crédite le bénéficiaire du même
  montant; le solde net global vaut zéro.
- Un lot `Rejected` est décrit comme invalide, même si aucune erreur de
  transaction n'a été trouvée.

## Compatibilité CSV

Le fichier v1.0 utilise cinq colonnes :
`id,sender,receiver,amount,type`. Les codes acceptés sont `VIR`, `PRE` et
`CHQ`. Le format historique à trois colonnes reste accepté. `parseLine` utilise
`0` comme sentinelle; `parseLines` attribue ensuite à chaque ligne historique
un identifiant distinct, supérieur au plus grand identifiant explicite. Ces
transactions reçoivent le type `Transfer` et le statut `Pending`.
