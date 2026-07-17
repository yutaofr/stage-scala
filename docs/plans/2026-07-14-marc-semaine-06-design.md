# Marc — Semaine 6 v1.1 Design

## Contrat retenu

La semaine 6 du dépôt enseigne la validation avancée, le pattern matching
exhaustif, les extracteurs, les for-comprehensions et les factories. Elle ne
porte pas encore sur `Either` ou `Validated`. La version v1.1 doit donc rendre
les erreurs lisibles et cumulables avec les ADT et les collections déjà
connues, puis laisser les limites d'`Option` visibles.

## Modèle et compatibilité

`Transaction` reste le modèle commun du fil rouge. La version v1.1 lui ajoute
les IBAN source et destination ainsi qu'une `Currency`; des valeurs par défaut
préservent les constructeurs S5. Le compagnon `Transaction.fromCsv` accepte le
format v1.1 à huit colonnes et vérifie la forme du CSV, l'identifiant, le
montant, le type et la devise. Le validateur traite ensuite les règles métier.

Cette séparation résout une tension du TP. `Iban.apply` doit accepter seulement
les valeurs marocaines valides, mais `FraudDetector` et `InternationalTx`
doivent observer des chaînes `XX` ou non `MA`. `Transaction` porte donc la
valeur d'entrée brute; `Iban` représente la preuve qu'une valeur de 24
caractères commence par `MA`. Le processor reçoit uniquement les transactions
dont l'évaluation ne contient aucune erreur.

## Hiérarchie d'erreurs

`ClearingError` se divise en `HighLevelError`, `LineError` et `SystemError`.
`LineError` se divise en `ValidationError` et `BusinessError`. Les erreurs S5
restent disponibles dans ces catégories; `FieldValidationError` remplace
l'ancienne case class plate `ValidationError`.

Un enum `Iso20022Code` modélise dix rejets fréquents : `AC01`, `AC04`, `AC06`,
`AG01`, `AM04`, `AM05`, `FF01`, `MD01`, `RC01` et `RR01`.
`Iso20022Rejection` les rattache à une transaction. Le reporter effectue un
match imbriqué et exhaustif sur les catégories, puis sur leurs cas concrets.

## Guards, extracteurs et apprentissage

Le validateur v1.1 emploie des gardes pour distinguer montant nul, montant
négatif, IBAN identiques et montant exact de 9 999,99 DH. Il utilise aussi le
pattern `Iban(_, bankSegment, _)` pour relier l'IBAN à la banque annoncée.
`FraudDetector`
extrait une raison au-dessus de 1 000 000 DH ou pour un IBAN `XX`.
`InternationalTx` reconnaît un IBAN source non marocain et
`InternationalFeePipeline` applique 2 % de frais depuis les lignes CSV. Cette
démonstration précède la validation stricte; le clearing principal reste
marocain et rejette l'international avant le netting.

`ErrorQueries` utilise des patterns dans des for-comprehensions pour extraire
les messages de validation et les codes métier. `FeePipeline` enchaîne les
trois fonctions `Option` demandées. `BatchValidationLab` expose l'adaptateur
tuple exact du cours; la chaîne v1.1 conserve des résultats nommés.

## Pipeline et rapport

`ClearingAppV11` parcourt les lignes avec leur numéro, appelle
`Transaction.fromCsv`, évalue chaque transaction une seule fois, sépare
succès, avertissements et erreurs, puis calcule le netting sur les seuls succès.
Les erreurs d'une même ligne restent groupées et sont toutes affichées.

Le fichier de démonstration contient 57 lignes : 45 succès, dont trois signaux
de fraude, et 12 lignes ignorées. Deux scénarios supplémentaires couvrent un
fichier vide et un fichier composé uniquement d'erreurs. Le rapport reste
déterministe, trie les banques et conserve l'invariant de netting global nul.
