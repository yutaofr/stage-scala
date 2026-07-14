# Semaine 11 — Polymorphisme ad-hoc et types opaques

## Objectif

Rendre le Clearing Engine v2.2 plus sûr et plus extensible. Le chemin actif
utilise `BankCode`, `Iban` et `Money` opaques du parser au netting. Le domaine
s'exporte en JSON, CSV ou XML par type classes, paramètres contextuels et
extension methods, sans dépendre d'un format technique.

## Notions à maîtriser

- Pattern Type Class et polymorphisme ad-hoc.
- Instances manuelles et séparation donnée/comportement.
- Paramètres contextuels `using` et instances `given`.
- Scope local, import explicite et ambiguïté de résolution.
- Extension methods sur types standards, Java et métier.
- Types opaques, factories sûres et coût runtime nul.
- `@targetName` après effacement de types JVM.
- `Numeric[Money]` pour `sum`, `max` et le netting.
- Migration incrémentale du domaine vers les bords.

## Exercices

- [x] J1 — Définir `ClearingSerializable[T]`, choisir manuellement des
  serializers CSV/JSON et sérialiser `LocalDateTime`.
- [x] J2 — Introduire `given/using`, comparer deux scopes de format et exporter
  des listes de banques et de transactions avec le même moteur générique.
- [x] J3 — Ajouter les extensions `isPositive`, `isValidBankCode`,
  `toSimpleFormat`, `toJson`, `toCsv`, `toXml`, `isValid` et `toSummary`.
- [x] J4 — Créer `BankCode`, `Iban` et `Money`, leurs factories,
  `@targetName`, les opérations monétaires, `Numeric` et les preuves de
  compilation négatives.
- [x] J5 — Assembler le railway v2.2, les trois formats, la démonstration de
  trois transactions et la rétrospective.

## Premier livrable

Le paquet `clearing.v22` contient un domaine opaque autonome. Le parser
transforme les huit colonnes en types métier; le validateur, le change, les
frais, le hash et le netting ne reçoivent plus de `String` ou `BigDecimal` à la
place d'une banque, d'un IBAN ou d'un montant.

La méthode contextuelle `export` de `ExportEngine` conserve une seule
implémentation. Le scope fournit un `JsonSerializer`, un `CsvSerializer` ou un
`XmlSerializer`; les classes métier restent indépendantes. Les trois formats
rendent les mêmes succès, rejets, positions, frais, warnings, statistiques et
somme globale.

## Critères de validation

- [x] `BankCode`, `Iban` et `IbanHash` sont opaques malgré leur représentation
  `String`.
- [x] `Money` est opaque malgré sa représentation `BigDecimal`.
- [x] Les factories normalisent ou retournent un `Either` explicite.
- [x] Les extensions `value` des trois opaques String utilisent `@targetName`.
- [x] `Numeric[Money]` active `sum` et `max`.
- [x] Les tests de compilation refusent `BankCode -> Iban`, un hash identité,
  une erreur de hash libre et les primitives brutes vers les quatre opaques.
- [x] Les instances manuelles fonctionnent sans résolution contextuelle.
- [x] Deux scopes `given` changent le format sans modifier `ExportEngine`.
- [x] Les extensions de format délèguent aux type classes.
- [x] Les extensions standards et métier restent pures.
- [x] Le parser constitue l'unique entrée des valeurs CSV brutes.
- [x] Les erreurs de parsing ne conservent ni la ligne ni l'IBAN invalide.
- [x] La validation agrège les règles et compare les segments IBAN typés.
- [x] Le railway court-circuite avant le hash et après son premier échec.
- [x] Les positions utilisent `Map[BankCode, Money]` et somment à `Money.zero`.
- [x] Le résultat et ses trois exports ne contiennent aucun IBAN brut.
- [x] JSON, CSV et XML ordonnent et échappent leurs données.
- [x] `MultiExportDemo` traite trois transactions sans dupliquer leur création.
- [x] `retro_s11.md` répond aux quatre questions du TP.
- [x] La suite S1–S11 passe sous Java 21.
- [x] La suite S1–S11 passe sous Java 17 Docker.
- [x] Le gate scoverage v2.0 reste à 100 % statement et branch.
- [x] Les trois formats et les deux scénarios d'échec passent sous les deux JDK.
- [x] L'audit statique, typage et confidentialité est propre.
- [x] La revue mentor ne conserve aucun point critique ou important.

## Journal TDD

- Les opaques ont commencé par un échec sur `DomainTypes`; onze tests couvrent
  factories, opérations, locale, `Numeric` et compilation négative.
- Le contrat contextuel a d'abord échoué sur les modèles et serializers absents.
  Le mot-clé réservé `export` impose des backticks autour du nom de la méthode
  contextuelle.
- Les extensions ont révélé une ambiguïté réelle entre `Money.isPositive` et
  `BigDecimal.isPositive`. Le scope du test masque explicitement l'une des deux.
- Le parser et le validateur ont commencé avec neuf comportements rouges :
  champs, priorité du premier échec, confidentialité, agrégation et doublon.
- Le railway a d'abord manqué entièrement, puis un test rouge a trouvé une
  devise EUR conservée après conversion MAD. `ConvertedTransaction` transporte
  maintenant la devise de référence.
- Les golden tests ont trouvé cinq écarts : warnings et global absents en JSON,
  global absent en CSV, compteurs/warnings/global absents en XML. Les formats
  conservent maintenant tout le résultat.
- La revue mentor a injecté un hash identité et reproduit une fuite d'IBAN.
  `HashBoundary` retourne maintenant un `IbanHash` validé ou un `HashFailure`
  fermé; les deux anciens rails non sûrs sont refusés à la compilation.

## Clarifications mentor

Le TP nomme une méthode `export`, mais Scala 3 réserve ce mot pour les clauses
d'export. Les backticks conservent le vocabulaire du cours sans renommer le
concept.

`BankCode.from` accepte trois ou quatre lettres parce que le référentiel du fil
rouge contient `SGMB`. L'extension pédagogique `String.isValidBankCode`
respecte néanmoins l'exercice exact de trois majuscules.

`unsafe` sert seulement aux constantes contrôlées et aux fixtures. Le parser et
les entrées externes utilisent toujours les factories sûres.

## Validation mentor

**Décision : S11 validée le 14/07/2026.**

- Java 21.0.6 : `sbt clean test`, 443 tests dans 67 suites, sans échec;
- Java 17.0.4.1 Docker : même suite, 443 tests dans 67 suites, sans échec;
- v2.2 ciblé : 52 tests dans neuf suites, sans échec;
- v2.0 : 49 tests dans six suites, couverture statement et branch à 100 %;
- JSON, CSV et XML : trois succès, quatre rejets et somme globale nulle sous
  les deux JDK;
- panne de hash : deux succès, cinq rejets, une erreur technique et somme
  globale nulle; fichier absent : erreur technique stable;
- audit : un seul `println` dans `V22Reporter`, un seul `partitionMap`, aucun
  IBAN littéral dans le code de production v2.2 et aucun effet de bord dans le
  cœur;
- revue senior finale : zéro point critique et zéro point important. Le point
  initial sur le hash identité est couvert par `IbanHash`, `HashFailure` et des
  preuves de compilation négatives.
