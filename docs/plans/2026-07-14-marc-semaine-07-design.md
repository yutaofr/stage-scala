# Marc — Semaine 7 v1.2 Design

## Contrat et choix d'architecture

La semaine 7 transforme le moteur validant de S6 en moteur de compensation.
Le nouveau paquet `clearing.v12` reçoit uniquement les transactions acceptées
par `ClearingAppV11`; il ne reparcourt pas les règles de validation. Cette
frontière préserve v1.1 et permet de comparer les résultats S1 à S7.

Trois architectures ont été considérées. Modifier `clearing.v11` réduirait le
nombre de fichiers, mais mêlerait validation et règlement. Généraliser toutes
les collections par typeclasses rendrait le moteur flexible, mais dépasserait
les objectifs de l'exercice. Le choix retenu crée des composants v1.2 nommés et
des adaptateurs pédagogiques. Ainsi, `BankPair` structure le code applicatif,
alors que l'API du TP expose encore `Map[(String, String), BigDecimal]`.
`MultilateralNetting.computePositions` retourne la Map par code demandée au TP;
`multilateralNetting` fournit aussi la variante `Map[Bank, BigDecimal]` du plan
hebdomadaire. Les deux méthodes partagent le même fold.

## Calculs bilatéraux et multilatéraux

`BilateralNetting` groupe d'abord les transactions validées avec `groupBy`, puis
agrège les montants avec `view.mapValues`. `getNetDuo(A, B)` calcule `A→B - B→A`.
Le rapport applicatif emploie `BilateralSettlement`, qui conserve un seul sens
par paire et porte un montant positif, un débiteur et un créancier.

`MultilateralNetting` parcourt le flux une fois avec `foldLeft` et `updatedWith`.
Chaque transaction débite l'émetteur et crédite le destinataire. La somme des
positions doit donc rester exactement nulle. Les tests couvrent les exemples
déterministes, les listes vides et cent jeux générés. Le rapport trie les
débiteurs du montant absolu le plus élevé au plus faible, puis les créditeurs du
plus élevé au plus faible.

Le modèle S7 reste mono-devise MAD, comme le précise le cours. Les fonctions de
netting n'effectuent aucune conversion implicite. `ClearingAppV12` documente ce
précontrat et utilise le fichier de démonstration MAD. Les frais, le collatéral,
le risque de liquidité et la réconciliation multi-devises restent hors scope.

## Segmentation, batch et détection de fenêtres

`FlowSegmentation` sépare les statuts avec `partition`, trie les transactions
par ID, découpe les lots avec `grouped` et analyse les fenêtres avec `sliding`.
La taille de batch est paramétrable : les exercices utilisent 10, tandis que la
chaîne v1.2 prend 1 000 par défaut. Chaque `BatchNetting` conserve son numéro,
sa taille et ses positions. `mergePositions` additionne les Maps partielles;
un test prouve que cette fusion égale le netting du flux complet.

Une `FraudWindowAlert` contient les cinq IDs et le montant total lorsque la
fenêtre dépasse 500 000 DH. L'ordre est déterministe, fondé sur l'ID, puisque le
modèle pédagogique ne porte pas encore de timestamp. Cette simplification suit
la note métier du cours.

`BusinessReporter` calcule le volume total et la banque la plus active en
comptant ses participations comme émettrice ou destinataire. Il représente les
messages par un ADT de logs, puis utilise `partition` pour séparer résultats
financiers et traces techniques.

## Performance, démonstration et preuves

`ScalabilityLab` génère un `Vector` déterministe pouvant atteindre un million
de transactions. Le benchmark compare List et Vector pour le netting, un
pipeline strict et sa variante `.view`, puis un calcul CPU séquentiel et sa
version `.par`. Scala 3 charge le module officiel
`scala-parallel-collections` 1.2.0. Les tests comparent toujours les résultats;
ils n'imposent aucune relation entre durées, car le temps dépend de la JVM et de
la machine.

Le benchmark d'un million est une commande de qualification, distincte de la
suite rapide. Son rapport consigne Java, volume, durées, mémoire maximale et
plus grand volume vérifié. Un essai sous limite mémoire Docker établit le seuil
observé sans risquer la machine hôte; le document distingue ce seuil du volume
théorique.

`ClearingAppV12` assemble la validation v1.1, les deux nettings, les batches,
les alertes et le rapport business. La démonstration de vendredi traite 100 000
transactions générées. L'acceptation exige les tests S1–S7 sous Java 21 et Java
17, le benchmark d'un million, l'invariant nul et une revue mentor indépendante.
