# Marc — Semaine 10 v2.1 Design

## But et contrainte de compatibilité

La semaine 10 remplace les échecs implicites du traitement par une voie ferrée
typée : chaque ligne devient un `Either[ClearingError, RailSuccess]`. Le nouveau
paquet `clearing.v21` reçoit le texte du batch et une configuration immuable,
puis retourne un rapport. Le netting ne voit que les succès.

La v2.1 s'ajoute à la v2.0. Elle ne modifie pas `Transaction.fromCsv`, dont le
retour `Option` constitue une preuve des semaines précédentes, et ne remplace
pas le pipeline pur S9. Les adaptations des frontières Java ajoutent des API
sûres tout en conservant les façades historiques. Ce choix protège les
349 tests et les démonstrations S1–S9.

Trois approches ont été comparées. Refactorer `clearing.v20` en place réduirait
la duplication, mais effacerait la progression pédagogique. Ajouter Cats
apporterait `Validated` et `bimap`, mais sortirait du périmètre du cours. Un
paquet `clearing.v21`, fondé sur l'`Either` standard, rend l'évolution visible
sans dépendance supplémentaire.

## Clarifications du cours

Le TP J3 cite `bimap`, absent de l'`Either` de la bibliothèque standard Scala 3.
La v2.1 utilise `fold` pour rendre les deux rails et `left.map` lorsque seule
l'erreur change. Cette solution respecte le cours J3 et évite une dépendance
Cats cachée.

Le cours promet que l'application « ne plante jamais ». Le contrat réalisable
porte sur les erreurs métier, les erreurs techniques attendues et les
exceptions `NonFatal`. Les erreurs fatales de la JVM, telles qu'un manque de
mémoire, restent hors du contrat. Une interruption restaure le drapeau
d'interruption avant de devenir une erreur technique.

## Modèle d'erreur

La hiérarchie scellée commune reçoit trois familles explicites :

- `ParsingError` nomme le numéro de ligne et la cause précise : nombre de
  colonnes, identifiant, montant, type ou devise;
- `TransactionValidationError` porte le numéro, l'identifiant éventuel et les
  règles métier violées;
- `TechnicalError` porte l'opération, le type d'exception et un message stable,
  sans conserver de `Throwable` dans le rapport.

Les erreurs de taux, de frais et de doublon restent des erreurs métier. Toutes
les erreurs exposent un code stable. Le renderer affiche ces codes et messages,
jamais une stack trace, un IBAN brut ou le contenu complet d'une ligne.

`LightWarning` représente un défaut récupérable. La récupération ne transforme
pas silencieusement un échec en succès : `RailTransaction` et `RailSuccess`
conservent une liste d'avertissements. L'exemple S10 récupère un libellé léger
absent avec une valeur par défaut; un montant, un IBAN, un taux ou un frais
invalide reste bloquant.

## Railway par ligne

Le pipeline traite une `NumberedLine` par un seul `for` :

```text
parse
  -> validate
  -> recoverLightWarning
  -> applyForex
  -> applyFee
  -> anonymize
  -> Right(RailSuccess)
```

Chaque étape retourne `Either[ClearingError, A]`. `flatMap` court-circuite la
ligne au premier échec bloquant. Le traitement des doublons reçoit l'ensemble
des identifiants déjà acceptés; seule la première occurrence progresse. Le
batch utilise `foldLeft` pour préserver l'ordre et cet état explicite.

Le hachage passe par une fonction injectée. La production appelle la frontière
Java sécurisée; les tests injectent un succès déterministe ou un échec
technique. Cette frontière permet de prouver qu'une panne Java rejette une
ligne sans interrompre les autres.

## Résultat de batch et statistiques

Le cœur conserve d'abord une `List[Either[ClearingError, RailSuccess]]`. Il la
sépare une seule fois avec `partitionMap`. Les succès alimentent le netting pur
S9; les erreurs alimentent des compteurs `parsing`, `validation`, `business` et
`technical`. Les avertissements restent attachés aux succès et possèdent leur
propre compteur.

Le rapport contient les résultats de ligne dans l'ordre d'entrée, les positions
triées, les frais, les statistiques et l'invariant global. Une ligne se rend
avec `fold` : `Transaction OK : MONTANT DEVISE` sur le rail droit ou
`REJET : CODE - RAISON` sur le rail gauche.

## Frontières `Try` et I/O

`SecurityUtils.hashIbanTry` encapsule `MessageDigest`; `hashIban` reste
disponible pour la v1.3 et la v2.0. `HttpExchangeRateService.fetchRateTry`
encapsule le client Java et distingue une réponse invalide d'un appel réussi;
`fetchRate` conserve son contrat `Option` historique.

`V21IO.read` utilise `Using(...).toEither` et transforme l'échec en
`TechnicalError`. Le point d'entrée entoure le traitement complet d'un
`Try`, convertit les exceptions `NonFatal`, puis confie tout affichage à
`V21Reporter`. Le cœur `clearing.v21` n'imprime rien et ne lit aucun fichier.

## Exercices quotidiens

Le J1 construit le parser `Either` et ses erreurs détaillées. Le J2 ajoute le
`for` complet et un exercice autonome `findAccount -> checkBalance ->
reserveFunds`. Le J3 prouve le rendu par `fold` et la récupération avec
avertissement. Le J4 sécurise les frontières Java et fichier avec `Try`. Le J5
assemble le batch v2.1, les statistiques et la démonstration corrompue.

L'exercice de compte utilise des valeurs immuables. `reserveFunds` retourne un
nouveau compte; un compte absent ou un solde insuffisant court-circuite la
réservation. Les tests observent aussi qu'aucun solde ne change après un échec.

## Démonstration et preuves

`transactions-v21.csv` contient des succès MAD/EUR, un avertissement récupéré
et des rejets de parsing, validation, doublon et taux. La démonstration doit
terminer avec un code de sortie normal, afficher les quatre catégories
d'erreurs, calculer les positions sur les seuls succès et préserver un solde
global nul. Aucun IBAN brut ne doit apparaître.

L'acceptation comprend :

1. les tests ciblés J1 à J5, écrits avant leur code de production;
2. la suite complète sous Java 21 et Java 17;
3. la couverture v2.0 à 100 % statement et branch, préservée comme garde de
   non-régression;
4. les démonstrations nominale, corrompue et fichier absent;
5. un audit statique des effets, des exceptions visibles et des données
   sensibles;
6. une revue mentor sans défaut bloquant.

Le jalon final porte la version `2.1.0-SNAPSHOT` et le tag local `marc-v2.1`.
