# Rétrospective S11 — Polymorphisme ad-hoc et types opaques

## 1. Concept le plus difficile

La frontière de transparence des types opaques m'a demandé le plus d'attention.
Dans `DomainTypes`, `BankCode` et `Iban` sont des `String`; hors de cet objet,
le compilateur les sépare. Cette différence explique à la fois leur coût nul à
l'exécution et le besoin de `@targetName` pour deux extensions `value` effacées
vers la même signature JVM.

La résolution des extensions a fourni un exemple concret. Le test importait à
la fois `Money.isPositive` et `BigDecimal.isPositive`; Scala a refusé l'appel
ambigu. J'ai conservé les deux exercices et masqué explicitement l'extension
opaque dans le scope qui testait la primitive.

## 2. Erreurs de compilation et fichier le plus difficile

J'ai observé dix compilations rouges volontaires pendant la migration. Je ne
compte pas chaque diagnostic, car un seul symbole absent produit souvent des
dizaines d'erreurs en cascade. Ces cycles ont couvert les opaques manquants,
les serializers, les extensions, le parser, le validateur, le railway et les
bords de l'application.

`Serializers.scala` a demandé le plus de corrections. `export` est un mot-clé
Scala 3; la méthode contextuelle de `ExportEngine` porte donc ce nom entre
backticks. Les golden tests ont ensuite révélé que le premier rendu du résultat
perdait les warnings et la somme globale. Les trois formats les conservent
désormais.

La revue mentor a ensuite trouvé un dixième cycle rouge : la frontière de hash
retournait une `String`, donc une implémentation identité pouvait réinjecter
l'IBAN brut. Un opaque `IbanHash` valide désormais la forme SHA-256 et
`HashFailure` interdit les messages d'erreur arbitraires à cette frontière.

## 3. Ordre de migration

Je garderais l'ordre choisi : opaques, domaine v2.2, type classes, extensions,
parser, validation, railway, puis application. Chaque étape possède une API
testable avant que la suivante ne la consomme. Le paquet versionné évite aussi
de transformer les erreurs pédagogiques des anciennes semaines en une migration
globale sans rapport avec S11.

Si je recommençais, j'ajouterais dès le premier test du railway une assertion
sur la devise de référence. Le premier calcul EUR produisait bien `108.00 MAD`,
mais transportait encore l'étiquette EUR. Le test a reproduit ce défaut avant
que `ConvertedTransaction` ne reçoive explicitement `referenceCurrency`.

## 4. Bug bancaire évité par les opaques

Une fonction qui reçoit trois `String` peut accepter par erreur un code bancaire
à la place d'un IBAN. Le code compile, puis le défaut apparaît au parsing, au
hash ou après l'envoi. En v2.2, `HashBoundary` exige un `Iban`; lui passer un
`BankCode` produit une erreur de compilation. Le même mécanisme empêche de
passer un `BigDecimal` de taux à la place d'un `Money` de règlement.
