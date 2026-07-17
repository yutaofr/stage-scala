# Semaine 12 — Monades et tests de propriétés

## Objectif

Assembler le Clearing Engine v2.3 avec les abstractions standard de Scala 3 :
`Functor`, `Monad`, `for-comprehension` et journal immutable. Renforcer ensuite
l'invariant bancaire central par des propriétés ScalaCheck sur 10 000 batchs
valides, sans introduire Cats, Pekko ou ZIO.

## Notions à maîtriser

- `Functor[F[_]]` et transformation `F[A] => F[B]` par `map`.
- Lois d'identité et de composition d'un Functor.
- `Monad[F[_]]`, `pure`, `flatMap` et dérivation de `map`.
- Accumulation chronologique d'un journal immutable.
- Différence entre le conteneur de log et le rail d'erreur `Either`.
- Désucrage d'un `for-yield` en `flatMap` puis `map`.
- Limite de composition directe entre deux monades différentes.
- Générateurs ScalaCheck construits par domaine plutôt que par filtrage.
- Propriétés, shrinking et différence entre test aléatoire et preuve formelle.

## Exercices

- [x] J1 — Définir `Functor`, les instances `List`, `Option`, `Box`, la
  fonction générique `transform` et les lois associées.
- [x] J2 — Définir `Monad`, `MonadicLogger`, `chain` et un laboratoire qui
  journalise le parsing, la validation et une sauvegarde observée.
- [x] J3 — Montrer l'équivalence entre `for-yield` et le chaînage explicite;
  faire refuser par le compilateur le mélange direct de `Option` et `Either`.
- [x] J4 — Générer des transactions valides sans `Gen.filter`; vérifier les
  lois et l'équilibre de 10 000 batchs de 200 transactions.
- [x] J5 — Assembler `V23Pipeline`, les trois exports, l'application, les
  scénarios d'échec et la rétrospective.

## Premier livrable

Le paquet `clearing.v23` conserve le domaine opaque et le railway v2.2. Il
ajoute ses abstractions pédagogiques sans bibliothèque fonctionnelle externe.
`V23Pipeline.runPure` compose quatre étapes par un seul `for-yield` sur
`MonadicLogger` : observation de l'entrée, clearing, audit du netting et export.

`V23Netting.positions` est le calcul actif de positions. Sa propriété principale
exécute 10 000 batchs de 200 transactions, soit 2 000 000 de transactions
générées, sans candidat rejeté par le générateur. Une régression dédiée vérifie
aussi qu'un auto-virement s'annule exactement.

Les effets restent aux bords : `V22IO` lit le fichier, la frontière de hash
appelle SHA-256 et `V23Reporter` contient l'unique `println` du paquet. Le cœur
retourne le rapport et son journal comme valeurs.

## Critères de validation

- [x] `Functor[F[_]]` expose `map` comme extension Scala 3.
- [x] `List`, `Option` et `Box` respectent identité et composition sur des
  valeurs générées.
- [x] `transform` fonctionne sans connaître le conteneur concret.
- [x] `Monad[F[_]]` étend `Functor[F]` et dérive `map` de `flatMap + pure`.
- [x] `MonadicLogger.flatMap` conserve l'ordre chronologique des traces.
- [x] Les identités gauche/droite et l'associativité sont vérifiées par
  propriétés à travers l'instance `given Monad[MonadicLogger]`.
- [x] Le laboratoire distingue accumulation des logs et court-circuit de
  `Either`.
- [x] Les formes `for-yield` et le désucrage littéral en `flatMap` imbriqués
  avec `map` final produisent les mêmes résultats.
- [x] Une preuve de compilation négative refuse `Either` puis `Option` dans le
  même `for`.
- [x] ScalaCheck 1.18.1 et le bridge ScalaTestPlus sont déclarés en test.
- [x] Les générateurs produisent directement six couples de banques distinctes
  et des centimes en `BigDecimal`; aucun `Gen.filter` ni `Double` n'est utilisé.
- [x] Un générateur direct vérifie 100 identifiants IBAN simulés de 24
  caractères sans filtrage.
- [x] 10 000 batchs de 200 transactions conservent une somme globale nulle.
- [x] 10 000 batchs supplémentaires correspondent, pour chaque banque, à
  l'oracle indépendant `crédits reçus - débits émis`.
- [x] Un auto-virement produit une position exactement nulle.
- [x] `V23Pipeline.runPure` utilise le netting soumis aux propriétés.
- [x] JSON, CSV et XML conservent trois succès, quatre rejets et un global nul.
- [x] La panne de hash produit deux succès, cinq rejets et une erreur technique
  sans interrompre le batch.
- [x] Un fichier absent produit une erreur technique stable.
- [x] Le journal est ordonné et ne contient ni IBAN, ni hash, ni ligne CSV.
- [x] Le rapport ne contient aucun IBAN brut.
- [x] Le cœur v2.3 reste silencieux; `V23Reporter` est la frontière console.
- [x] La suite S1–S12 passe sous Java 21.
- [x] La suite S1–S12 passe sous Java 17 Docker.
- [x] Le gate scoverage v2.0 reste à 100 % statement et branch.
- [x] Les cinq démonstrations passent sous les deux JDK.
- [x] L'audit statique est propre.
- [x] La revue mentor ne conserve aucun point critique ou important.

## Journal TDD

- Les tests J1 ont d'abord échoué sur `Functor`, `Box` et `transform` absents.
- Les tests J2 ont d'abord échoué sur `Monad`, `MonadicLogger` et les étapes du
  laboratoire absentes.
- Les tests J3 ont d'abord échoué sur `ForEquivalence`; la preuve négative
  documente ensuite l'incompatibilité directe de deux conteneurs.
- Les tests J4 ont d'abord échoué sur les imports ScalaCheck et
  `V23Netting`. Après le premier GREEN, la lecture du contrat a révélé que le
  générateur principal excluait les auto-virements. Une régression rouge a
  reproduit une position `+125.75`; le débit puis le crédit sont maintenant
  appliqués séquentiellement.
- Les tests J5 ont d'abord échoué sur le pipeline, l'application et le reporter
  absents. Une erreur de compilation a rappelé que `export` est réservé en
  Scala 3; la méthode conserve ses backticks.

## Portée de la preuve

Les 10 000 essais explorent beaucoup plus de combinaisons que des exemples
manuels et fournissent un cas réduit en cas d'échec. Ils ne parcourent pas
toutes les listes, tous les montants et tous les états possibles. La validation
atteste donc une forte confiance expérimentale dans l'invariant; elle ne
constitue pas une preuve formelle exhaustive.

## Validation mentor

**Décision : S12 validée le 14/07/2026.**

- Java 21.0.6 : `sbt clean test`, 477 tests dans 75 suites, sans échec;
- Java 17.0.4.1 Docker : même suite, 477 tests dans 75 suites, sans échec;
- v2.3 ciblé : 34 tests dans huit suites après corrections de revue; les deux
  propriétés de netting exécutent chacune 10 000 batchs de 200 transactions;
- v2.0 : 49 tests dans six suites, couverture statement et branch à 100 %;
- JSON, CSV et XML : trois succès, quatre rejets et somme globale nulle sous les
  deux JDK;
- panne de hash : deux succès, cinq rejets, une erreur technique et somme
  globale nulle; fichier absent : erreur technique stable;
- audit : un seul `println` dans `V23Reporter`, aucun effet de bord dans
  `V23Pipeline`, aucun générateur filtré, aucun `Double`, aucune dépendance
  Cats/Pekko/ZIO et aucun IBAN brut dans le code de production v2.3;
- revue senior initiale : deux points importants sur le désucrage littéral et
  la faiblesse de l'invariant global; les deux ont reçu un cycle RED/GREEN;
- revue senior finale : zéro point critique et zéro point important. Le point
  mineur de dérive du design vers l'ancien oracle v2.2 a aussi été corrigé.
