# Marc — Semaine 11 v2.2 Design

## But et contrainte de compatibilité

La semaine 11 livre un Clearing Engine v2.2 dont le cœur distingue les codes
bancaires, les IBAN et les montants à la compilation. Elle ajoute aussi un
export JSON, CSV ou XML sans placer ce comportement dans les classes métier.
Le même `export[T]` fonctionne pour une banque, une transaction ou un résultat
de clearing dès que le contexte fournit le serializer demandé.

La v2.2 s'ajoute aux jalons précédents dans `clearing.v22`. Elle conserve les
API, les exécutables et les tests S1–S10. Modifier la `Transaction` partagée
forcerait une migration simultanée de plus de soixante-dix sources et
effacerait la progression pédagogique. Le paquet versionné applique néanmoins
la migration complète au chemin actif v2.2 : aucune banque, aucun IBAN et aucun
montant brut ne traverse son cœur.

Trois approches ont été comparées. Une migration globale du modèle commun
respecterait littéralement le chemin proposé par le TP, mais casserait les
preuves historiques. Des wrappers `case class` préserveraient le projet, mais
manqueraient l'objectif explicite des types opaques. Un domaine v2.2 autonome,
relié aux énumérations stables `Currency`, `TransactionType` et
`TransactionStatus`, satisfait le contrat S11 et garde chaque ancien jalon
exécutable.

## Domaine opaque

`DomainTypes.scala` définit trois types :

- `BankCode`, représenté par une `String`, accepte un code normalisé de trois
  ou quatre lettres majuscules;
- `Iban`, représenté par une `String`, accepte vingt-quatre caractères, le
  pays `MA` et un segment bancaire exploitable;
- `Money`, représenté par un `BigDecimal`, conserve la précision décimale et
  fournit `+`, `-`, `*`, `abs`, `isPositive`, `format` et `value`.

Chaque compagnon expose une factory sûre qui retourne `Either[String, A]`.
`unsafe` sert exclusivement aux fixtures et aux configurations constantes; il
appelle la factory et échoue immédiatement si la constante est invalide. Les
extensions `value` de `BankCode` et `Iban` portent des `@targetName` distincts,
car la JVM efface les deux opaques vers `String`. `Money` fournit un
`Numeric[Money]`, donc `sum`, `max` et le netting restent génériques.

`V22Domain.scala` définit `Bank`, `Transaction`, `PreparedTransaction`,
`ClearingResult` et `V22Config`. Le modèle emploie les opaques dans chaque
champ, collection et signature concernés. Une transaction ne peut donc pas
recevoir un IBAN à la place d'un code bancaire ou un `BigDecimal` brut à la
place d'un montant. Un test fondé sur `scala.compiletime.testing` prouve ces
refus de compilation.

## Type classes et résolution contextuelle

`ClearingSerializable[T]` expose `toText(value: T)`. Les traits
`JsonSerializer[T]`, `CsvSerializer[T]` et `XmlSerializer[T]` spécialisent ce
contrat sans modifier le domaine. Trois objets de format publient leurs
instances `given` pour `Bank`, `Transaction` et `ClearingResult`.

`ExportEngine.export[T]` et `exportBatch[T]` ne connaissent aucun format. Ils
demandent un `ClearingSerializable[T]` avec `using`. Le format devient un choix
local et explicite : un bloc importe les instances JSON, un autre les instances
CSV ou XML. Cette structure permet de démontrer la priorité du scope local et
évite un serializer global ambigu.

Les serializers ordonnent leurs champs et leurs collections. JSON échappe les
guillemets et les caractères de contrôle; CSV protège les virgules, les
guillemets et les retours à la ligne; XML échappe les cinq caractères réservés.
Ils n'exportent jamais un IBAN brut. Les transactions exposent seulement leurs
hashes dans un résultat de clearing.

## Extension methods et DSL

`Syntax.scala` regroupe quatre familles d'extensions :

- `BigDecimal.isPositive` et `String.isValidBankCode` matérialisent l'exercice
  sur les types standards;
- `LocalDateTime.toSimpleFormat` produit le préfixe déterministe `dd/MM`;
- `T.toJson`, `T.toCsv` et `T.toXml` demandent la type class correspondante;
- `Transaction.isValid` et `Transaction.toSummary` expriment le DSL métier.

Les extensions restent des fonctions pures. Elles ne lisent aucun fichier et
n'impriment rien. Le validateur v2.2 utilise les méthodes de `Money`,
`BankCode` et `Iban` afin que ses règles lisent le domaine plutôt que ses types
sous-jacents.

## Railway v2.2

Le parser constitue l'unique entrée de valeurs brutes. Il transforme chaque
ligne en `Either[V22Error, Transaction]` et appelle les factories opaques pour
les banques, les IBAN et le montant. Le cœur traite ensuite une ligne par ce
rail :

```text
parse opaque
  -> validate typed transaction
  -> recover optional label
  -> convert Money
  -> calculate Money fee
  -> hash Iban at the injected boundary
  -> Right(PreparedTransaction)
```

`V22Error` distingue parsing, validation, doublon, configuration et technique.
Le batch préserve l'ordre, accepte seulement le premier identifiant réussi et
compense uniquement les succès. Ses positions utilisent
`Map[BankCode, Money]`; leur somme doit valoir `Money.zero`. Le hash reçoit un
`Iban`, puis extrait sa valeur uniquement dans l'adaptateur Java.

`ClearingResult` contient les succès préparés, les rejets, les positions, les
frais et les compteurs. Il ne contient aucun IBAN. Les trois serializers
peuvent donc rendre le même résultat sans risque de fuite.

## Application et démonstrations

`ClearingAppV22` lit `transactions-v22.csv`, exécute le cœur et choisit le
serializer demandé par la commande. Le CLI accepte un format JSON, CSV ou XML,
un chemin facultatif et le mode explicite `--simulate-hash-failure`. Le cœur
retourne une valeur; `V22Reporter` constitue l'unique frontière console.

`MultiExportDemo` construit trois transactions typées une seule fois, les
traite, puis exporte le même résultat dans les trois formats. La démonstration
prouve que seul le contexte de sérialisation change. Elle affiche aussi une
banque et une transaction avec le même `ExportEngine` générique.

## Exercices quotidiens

Le J1 crée `ClearingSerializable`, les instances manuelles CSV/JSON et
l'instance `LocalDateTime`. Le J2 convertit ces instances en `given`, introduit
`using`, compare les scopes et exporte des listes. Le J3 ajoute les extensions
de validation, de date et de sérialisation. Le J4 crée les opaques, leurs
factories, leurs opérations, `Numeric[Money]` et les preuves de compilation.
Le J5 assemble le railway v2.2, ajoute XML, livre la démonstration multi-format
et consigne la rétrospective de Marc.

## Preuves et critères d'acceptation

L'acceptation exige :

1. des cycles RED/GREEN observés pour chaque nouveau comportement;
2. des tests de compilation positifs et négatifs pour les trois opaques;
3. des tests des factories, opérations, `Numeric` et `@targetName`;
4. des tests des instances manuelles, des `given/using`, du scope local et des
   extensions;
5. des golden tests déterministes pour JSON, CSV et XML, y compris leur
   échappement;
6. des tests du parser, de la validation, du court-circuit, du netting et de
   l'absence d'IBAN dans `ClearingResult`;
7. la suite S1–S11 sous Java 21 et Java 17;
8. le gate scoverage v2.0 à 100 % statement et branch;
9. les démonstrations v2.2 dans les trois formats, le mode technique et le
   fichier absent;
10. un audit statique qui localise `.value`, `unsafe`, `println` et les effets;
11. une revue mentor sans défaut critique ou important.

Le jalon final portera la version `2.2.0-SNAPSHOT` et le tag local
`marc-v2.2`. La branche restera locale tant que le tuteur ne demande pas de
push.
