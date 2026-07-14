# Semaine 9 — Pureté, composition et Clearing Engine v2.0

## Objectif

Séparer strictement la logique de clearing de ses effets de bord, puis
construire un moteur v2.0 par composition de fonctions pures. Le même batch et
la même configuration doivent toujours produire le même rapport et les mêmes
octets, sans sortie console depuis le cœur.

## Notions à maîtriser

- Transparence référentielle et audit d'un effet de bord.
- Fonction pure, valeur immutable et raisonnement local.
- Retour d'une valeur au lieu d'une mutation ou d'un affichage.
- Fonction comme valeur, `andThen`, `compose` et ordre d'exécution.
- Currying, application partielle et fonction spécialisée.
- Configuration immutable injectée dans un pipeline.
- Séparation cœur fonctionnel / coquille impérative.
- Déterminisme de l'objet métier et de son rendu UTF-8.

## Exercices

- [x] J1 — Auditer v1.3, localiser fichier, console, HTTP, temps et UUID, puis
  définir `ClearingReporter` comme frontière d'affichage.
- [x] J2 — Écrire un `PureNettingCalculator` sans mutation, prouver la somme
  globale nulle et répéter le même calcul 1 000 fois.
- [x] J3 — Écrire les micro-fonctions de nettoyage, l'`Either` demandé,
  l'anonymisation et comparer `andThen` à `compose`.
- [x] J4 — Curryfier logger, limite et frais; préconfigurer MAD et EUR et
  sélectionner des fonctions de frais dans une `Map`.
- [x] J5 — Composer parsing, validation, préparation et netting; isoler lecture
  et affichage; exécuter les deux profils sur le même fichier.

## Premier livrable

Une version `v2.0` logic-only qui reçoit du texte CSV et un
`PureEngineConfig`, retourne un `PureClearingReport` immutable, conserve les
rejets dans l'ordre des lignes, ne garde que les hashes d'IBAN et rend des
positions équilibrées. `IOBridge` et `ClearingReporter` forment la coquille
impérative minimale.

## Critères de validation

- [x] L'audit S9 identifie chaque effet de bord hérité de v1.3.
- [x] Les types de batch conservent numéros de ligne, ordre et rejets.
- [x] `validateStatus` retourne l'`Either` demandé sans lancer d'exception.
- [x] `DataCleaner` nettoie, arrondit et anonymise sans conserver d'IBAN brut.
- [x] `andThen` et `compose` ont un comportement distinct démontré par test.
- [x] Les limites sont strictes et spécialisées par currying.
- [x] Les frais de production utilisent `BigDecimal`; l'adaptateur pédagogique
  `Double` passe par `BigDecimal.valueOf`.
- [x] Les taux de change acceptés sont strictement positifs et les taux de
  frais sont positifs ou nuls.
- [x] MAD et EUR utilisent le même cœur avec deux configurations immutables.
- [x] Le netting utilise uniquement le principal; les frais restent séparés.
- [x] Les positions MAD et EUR ont toutes deux une somme globale de `0.00`.
- [x] CSV malformé, montant non positif, limite atteinte, règle métier et taux
  manquant deviennent des rejets sans interrompre le batch.
- [x] Un ID dupliqué est rejeté; ses autres erreurs restent accumulées.
- [x] Une limite ou un taux de frais absent fait échouer la ligne fermée; un
  taux de frais négatif est également rejeté.
- [x] Les rejets restent triés par numéro de ligne.
- [x] Le rapport ne contient ni IBAN brut, ni temps, ni UUID.
- [x] Le renderer trie les maps avant de produire ses lignes.
- [x] Mille appels produisent le même objet et les mêmes octets UTF-8.
- [x] La normalisation utilise `Locale.ROOT`; la locale JVM turque ne change
  ni le parsing, ni le rapport, ni la sélection du profil.
- [x] Le cœur n'écrit rien sur stdout; seul `ClearingReporter` affiche.
- [x] `IOBridge` transforme une lecture impossible en valeur nommée.
- [x] Le gate scoverage du cœur pur impose et atteint 100 % des statements et
  100 % des branches.
- [x] Les tests v2.0 et v1.3 passent ensemble sous Java 21.
- [x] Toute la suite S1 à S9 passe sous Java 21 après la revue.
- [x] Toute la suite S1 à S9 passe sous Java 17 Docker après la revue.
- [x] Les démonstrations MAD et EUR passent dans les deux environnements après
  la revue.
- [x] L'audit statique final du cœur pur est propre.
- [x] La revue mentor finale ne conserve aucun point critique ou important.

## Journal d'exécution

### Cycles TDD observés

- Les nettoyeurs ont commencé par huit symboles absents; sept tests ont fixé
  normalisation, arrondi, `Either`, copie immutable et ordre de composition.
- Les règles curryfiées ont commencé par dix symboles absents; sept tests ont
  fixé limite stricte, frais, log retourné, fonctions en `Map` et profils.
- Le netting a commencé par huit erreurs de compilation; cinq tests prouvent le
  fold immutable, le filtrage de statut, le batch vide, la somme nulle et
  1 000 résultats identiques.
- Une première revue mentor a détecté les types de batch et la frontière de
  confidentialité encore absents. Un nouveau test a d'abord produit treize
  erreurs de compilation; quatre tests couvrent maintenant ordre, erreurs,
  hashes fixes et absence de champs IBAN. La seconde revue ferme ce point.
- Le moteur composé a commencé par treize symboles absents; huit tests couvrent
  les étapes, MAD, EUR, taux manquant, composition exacte, tri, confidentialité
  et déterminisme objet/octets.
- La coquille I/O a commencé par vingt-et-une erreurs de compilation; dix tests
  couvrent CLI, lecture, échec nommé, deux profils, compteurs et reporter.
- La revue finale a d'abord reproduit une erreur de compilation introduite par
  une indentation Scala 3 incorrecte. Le test ciblé a confirmé les quatorze
  erreurs avant correction.
- Trois RED supplémentaires ont reproduit l'acceptation d'un ID dupliqué, la
  gratuité silencieuse d'un taux de frais absent et la dépendance à la locale
  turque. Les corrections conservent les erreurs, échouent fermées et utilisent
  `Locale.ROOT`.
- Le premier rapport scoverage mesurait 88,79 % des statements et 72,55 % des
  branches. Les tests de cumul, erreurs combinées et codes d'erreur portent
  maintenant les deux mesures à 100,00 %, avec seuils bloquants.

### Décisions mentor à confirmer

V2.0 vit dans `clearing.v20` au lieu de modifier v1.3. La preuve S8 connectée
reste donc exécutable, tandis que S9 peut exclure HTTP, Spring, temps et UUID de
son cœur.

Le TP J4 présente `Double`; seule la petite fonction pédagogique l'accepte.
Les profils et calculs financiers utilisent `BigDecimal`. Les frais sont une
commission de la banque émettrice et ne créditent jamais le bénéficiaire.

Le log fonctionnel retourne une chaîne. Le cours complet demande un cœur sans
effet : l'impression montrée dans un exercice isolé est donc repoussée dans
`ClearingReporter`.

La propagation complète avec `Either` et `flatMap` appartient à S10. S9 expose
l'`Either` exact demandé par le TP, mais garde des listes nommées de succès et
de rejets entre les étapes afin de rendre l'ordre visible aux débutants.

## Validation mentor

**Décision : acceptée.** Le 14/07/2026, la suite complète compile 65 sources
Scala et deux sources Java de production, puis 50 sources de test. Elle exécute
349 tests dans 50 suites, sans échec, sous Java 21 local et Java 17 Docker.

Le gate scoverage exécute 49 tests v2.0 dans six suites et atteint 100,00 % des
statements ainsi que 100,00 % des branches des six fichiers du cœur pur. Les
seuils sont inscrits dans `build.sbt` et rendent la commande non réussie sous
100 %.

Dans les deux environnements, les démonstrations MAD et EUR lisent sept lignes,
en acceptent trois, en rejettent quatre et terminent avec `GLOBAL|0.00`. Les
rapports observés contiennent les hashes SHA-256, jamais les IBAN bruts.

La revue mentor finale a reproduit puis fait corriger l'indentation Scala 3,
les IDs dupliqués, les configurations fail-open, la locale JVM implicite et les
accumulations quadratiques. La dernière relecture ne conserve aucun point
critique, important ou mineur. L'audit de pureté et `git diff --check` sont
propres. Le jalon peut recevoir le tag `marc-v2.0`.
