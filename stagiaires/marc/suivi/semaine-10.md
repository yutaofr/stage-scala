# Semaine 10 — Either, Try et Railway Oriented Programming

## Objectif

Faire traverser chaque ligne du Clearing Engine v2.1 sur une voie ferrée
typée. Parsing, validation, récupération, change, frais et anonymisation
retournent tous un `Either`; le premier `Left` bloque seulement la ligne
concernée. Le netting reçoit exclusivement les `Right`.

## Notions à maîtriser

- `Either[E, A]` et convention rail gauche / rail droit.
- `map`, `flatMap` et désucrage d'une for-comprehension.
- Court-circuit au premier échec bloquant.
- `fold` pour consommer les deux rails.
- Récupération limitée, visible par un avertissement.
- `Try` à une frontière Java ou I/O, puis conversion en `Either` métier.
- `List[Either]`, `partitionMap` et statistiques par catégorie.
- Capture de `NonFatal`; les erreurs fatales de la JVM restent hors contrat.

## Exercices

- [x] J1 — Parser huit colonnes avec des erreurs précises de colonne, ID,
  montant, type et devise.
- [x] J2 — Composer validation et réservation de compte avec `flatMap` et un
  `for`; préserver les valeurs immuables après un échec.
- [x] J3 — Récupérer seulement un libellé optionnel absent, conserver un
  warning et rendre les deux rails avec `fold`.
- [x] J4 — Ajouter `hashIbanTry`, `fetchRateTry` et `V21IO.read`; restaurer le
  drapeau d'interruption Java.
- [x] J5 — Traiter une liste d'`Either`, partitionner une fois, calculer les
  statistiques et netter seulement les succès.

## Premier livrable

Le paquet `clearing.v21` ajoute un parser détaillé, une validation typée, une
récupération explicite, un pipeline par ligne écrit en `for`, un rapport de
batch et une coquille I/O. Les API publiques et le comportement de
`Transaction.fromCsv` et `clearing.v20` restent compatibles; seuls leurs
adaptateurs de match exhaustif ont évolué pour couvrir la hiérarchie scellée
étendue.

Le scénario `--simulate-hash-failure` traite sept lignes : deux succès, cinq
rejets et un avertissement. Il produit une erreur de parsing, une de
validation, deux métier et une technique, puis termine avec un solde global
de `0.00`.

## Critères de validation

- [x] Le parser ne retourne plus `None`; chaque cause syntaxique est typée.
- [x] Une erreur de parsing ne conserve pas la ligne CSV complète.
- [x] La validation agrège les règles d'une ligne dans un seul `Left`.
- [x] Un ID déjà accepté devient une erreur métier `AM05`.
- [x] L'exercice de compte suit `findAccount -> checkBalance -> reserveFunds`.
- [x] Une réservation échouée ne modifie ni la `Map`, ni le compte initial.
- [x] La récupération ne concerne que le libellé optionnel.
- [x] Chaque récupération ajoute `LightWarning.MissingLabel`.
- [x] Les montants, IBAN, taux et frais invalides restent bloquants.
- [x] Le renderer utilise `Either.fold`; aucun `bimap` non standard n'est
  introduit.
- [x] Les frontières Java exposent `Try` tout en conservant les façades S8.
- [x] Une interruption Java devient `Failure` et restaure le drapeau.
- [x] La lecture fichier retourne `Either[TechnicalError, String]`.
- [x] Le pipeline complet emploie un seul `for` par ligne.
- [x] Parsing, validation, forex et frais court-circuitent avant le hash.
- [x] Une panne du premier hash empêche l'appel du second.
- [x] Les résultats gardent l'ordre et partitionnent les rails une seule fois.
- [x] Les erreurs sont comptées en parsing, validation, business et technical.
- [x] Seuls les succès alimentent frais et netting.
- [x] Les positions du scénario sont ATH `+8.00` et CIH `-8.00`.
- [x] Le rapport ne contient aucun IBAN brut ni stack trace.
- [x] Mille traitements identiques retournent le même objet.
- [x] La coquille capture `NonFatal`, mais laisse une erreur fatale remonter.
- [x] Seul `V21Reporter` imprime dans le paquet v2.1.
- [x] La suite S1–S10 passe sous Java 21 après implémentation.
- [x] La suite S1–S10 passe sous Java 17 Docker après implémentation.
- [x] Le gate scoverage v2.0 reste à 100 % statement et branch.
- [x] Les deux démonstrations passent sous Java 21 et Java 17.
- [x] L'audit statique et confidentialité final est propre.
- [x] La revue mentor finale ne conserve aucun point bloquant.

## Journal TDD

- J1 a d'abord échoué sur sept références absentes. Quatre tests couvrent le
  succès, les cinq causes syntaxiques et la priorité du premier `Left`.
- L'extension du trait scellé a révélé deux matches historiques incomplets.
  Deux tests ont reproduit les `MatchError` avant leur correction.
- J2 a commencé par onze symboles absents. Huit tests couvrent la validation,
  les limites, la duplication et la réservation immutable.
- J3 a commencé par onze symboles absents. Sept tests couvrent récupération,
  erreurs non récupérées, `fold`, confidentialité et rendu des warnings.
- J4 a commencé par neuf APIs absentes. Les tests réels couvrent SHA-256,
  serveur HTTP, réponse invalide, port inaccessible, interruption et fichier.
- J5 a commencé par quinze symboles absents. Six tests couvrent les étapes,
  appels de hash, partition, statistiques, ordre, batch vide et déterminisme.
- La coquille a commencé par douze symboles absents. Sept tests couvrent CLI,
  scénario corrompu, fichier absent, silence du cœur, `NonFatal` et reporter.
- La revue mentor a déclenché trois nouveaux cycles rouges puis verts : deux
  tests de cohérence IBAN/banque, un test de confidentialité du renderer et un
  test de stabilité des codes techniques sous locale turque.

## Clarifications mentor

Le TP J3 demande `bimap`, mais l'`Either` de la bibliothèque standard Scala 3
n'en fournit pas. Le projet utilise `fold` pour consommer les deux rails et
`left.map` lorsque seule l'erreur change. Ajouter Cats à ce stade masquerait
l'objectif pédagogique.

« Ne plante jamais » signifie ici que les erreurs métier, techniques attendues
et exceptions `NonFatal` deviennent des valeurs. Une erreur fatale de la JVM,
par exemple `OutOfMemoryError`, n'est pas interceptée.

La panne de hash est un mode de démonstration explicite. Le mode normal appelle
SHA-256; il ne fabrique pas une erreur technique pour gonfler les statistiques.

## Validation mentor

**Décision finale : semaine 10 validée.** La suite compile 74 sources Scala et
deux sources Java de production, puis 58 sources de test. Elle exécute 391
tests dans 58 suites, sans échec, sous Java 21.0.6 local et Java 17.0.4.1
Docker.

Le gate v2.0 exécute 49 tests dans six suites et conserve 100,00 % des
statements ainsi que 100,00 % des branches. Dans les deux JDK, le scénario
simulé rend deux succès, cinq rejets, un warning, les quatre catégories et
`GLOBAL|0.00`; le fichier absent devient `TECH_READ_FILE` avec un code de
sortie normal.

L'audit trouve un seul `println`, dans `V21Reporter`, et un seul
`partitionMap`, dans `RailwayEngine`. Le cœur ne contient ni accès fichier ou
réseau, ni mutation, ni `throw`; les sources v2.1 ne contiennent aucun IBAN de
démonstration. La revue indépendante a d'abord relevé trois points importants :
cohérence entre banque et segment IBAN, confidentialité des anciennes erreurs
de validation et stabilité des codes sous locale turque. Les trois corrections
ont été couvertes par des tests rouges puis verts. La seconde lecture conclut à
zéro point critique ou important; les trois remarques documentaires mineures
ont également été corrigées avant la validation.
