# Marc — Semaine 9 v2.0 Design

## Contrat et choix d'architecture

La semaine 9 transforme le moteur en cœur fonctionnel. Le paquet
`clearing.v20` reçoit du texte et une configuration immuable, puis retourne un
rapport. Il ne lit aucun fichier, n'appelle aucun service, ne consulte aucune
horloge et n'affiche rien. `IOBridge` conserve les effets nécessaires à la
démonstration.

Trois options ont été étudiées. Modifier `clearing.v13` en place réduirait le
nombre de fichiers, mais détruirait les preuves Java, Spring et HTTP validées en
S8. Une algèbre d'effets générique séparerait aussi le cœur des interpréteurs,
mais elle anticiperait les abstractions des semaines suivantes. Le nouveau
paquet `clearing.v20` constitue le compromis pédagogique : il réutilise le
domaine stable et garde v1.3 intact.

Le cours contient deux contradictions. Il qualifie de pure une fonction qui
appelle `println`, puis exige un cœur sans I/O. Il demande aussi un taux de frais
`Double`, alors que le projet utilise `BigDecimal` pour les calculs financiers.
Le code résout ces contradictions explicitement. Le cœur retourne des messages
de trace; `ClearingReporter` les affiche au bord. L'adaptateur pédagogique
`applyFee(Double)` convertit le taux avec `BigDecimal.valueOf`, tandis que le
pipeline utilise des taux `BigDecimal`.

## Audit de pureté

Le rapport `audits/semaine-09.md` classe les effets de v1.3 :

- `ClearingAppV13` lit le fichier, choisit l'horloge et l'UUID, crée le client
  HTTP et affiche le rapport;
- `HttpExchangeRateService` et `LocalExchangeRateServer` exécutent le réseau;
- `SecureBatchFactory` reste déterministe lorsque l'appelant injecte un
  `Clock` et un fournisseur d'UUID;
- `JavaCollectionAdapters.liveView` expose une mutation Java contrôlée;
- le netting v1.2 et les règles de validation sont déjà des transformations
  pures.

L'audit distingue le code historique des points d'entrée actifs. Les anciens
exercices conservent leurs `println` pour la non-régression; v2.0 ne les appelle
pas. Le contrôle final cherche `println`, `Console`, `HttpClient`, Spring,
`Clock`, UUID, `var`, `throw` et collections mutables dans `clearing.v20`. Seuls
`IOBridge` et `ClearingReporter` peuvent contenir des effets.

## Domaine pur et configuration

`PureEngineConfig` porte la devise de référence, les limites par type, les taux
de frais par banque et les taux de change vers la référence. Deux profils
préconfigurés, `clearingMAD` et `clearingEUR`, utilisent la même logique.

`RawTransaction` conserve le numéro et la ligne CSV. `PreparedTransaction`
contient seulement les données nécessaires au règlement : identifiant, banques,
montant converti, type, statut, devise de référence, frais et hashes d'IBAN. Le
rapport final ne conserve aucun IBAN brut.

`PureRejection` décrit un rejet sans lancer d'exception. S9 utilise des listes
de succès et de rejets dans les états intermédiaires. `DataCleaner.validateStatus`
expose toutefois l'`Either` exact demandé par le TP quotidien. Le pipeline
complet n'utilise pas encore `flatMap` pour propager ce type : cette composition
constitue l'objectif S10.

Les frais restent séparés du montant de règlement. Ils représentent la
commission de la banque émettrice et ne créditent pas la banque bénéficiaire.
Le netting utilise donc le principal converti; le rapport additionne les frais
à part.

## Fonctions et composition

`DataCleaner` expose les briques du J3 :

- `cleanIban: String => String` supprime les espaces et met en majuscules;
- `formatAmount: BigDecimal => BigDecimal` arrondit à deux décimales;
- `validateStatus: Transaction => Either[PureValidationError, Transaction]`
  rejette un montant nul ou négatif;
- `cleanTransaction` compose les nettoyages;
- `anonymize` produit un `PreparedTransaction` sans IBAN brut;
- des exemples `andThen` et `compose` prouvent que l'ordre change le résultat.

`CurriedRules` expose `checkLimit`, `applyFee`, `applyPreciseFee` et
`logWithBank`. Les profils spécialisés fixent les limites et les taux une seule
fois. `logWithBank` retourne une chaîne; l'adaptateur d'I/O peut l'imprimer.

`PureClearingEngine.configure(config)` construit le pipeline :

```text
String CSV
  -> List[RawTransaction]
  -> ParsedBatch
  -> ValidatedBatch
  -> PreparedBatch
  -> PureClearingReport
  -> String déterministe
```

Chaque flèche est une valeur fonction. Le pipeline assemble les flèches avec
`andThen`. Les étapes conservent l'ordre des lignes et accumulent les rejets.

## Netting, anonymisation et déterminisme

`PureNettingCalculator` calcule les positions avec `foldLeft` et `updatedWith`.
Il ignore les statuts rejetés et n'utilise ni mutation, ni sortie, ni exception.
Un test appelle la même fonction mille fois et compare chaque résultat au
premier.

L'anonymisation réutilise le SHA-256 déterministe de S8. Le rapport garde les
hashes, mais aucun UUID ou horodatage : ces valeurs empêcheraient deux exécutions
du même batch de produire le même objet. La v1.3 reste la version connectée et
horodatée; la v2.0 est la version logic-only.

Le renderer trie les positions par code bancaire et conserve l'ordre des
transactions et rejets. Un test exécute le pipeline mille fois, compare les
objets, puis compare les octets UTF-8 des rapports.

## Bords d'I/O et démonstration

`IOBridge` lit un fichier avec `Using`, sélectionne un profil MAD ou EUR et
appelle le pipeline pur. `ClearingReporter` contient les seuls `println` de
v2.0. L'application principale prépare la configuration avant de traiter le
fichier, conformément au TP J5.

La démonstration traite `transactions-v20.csv` dans les deux profils. Elle
montre le même ensemble d'IDs, des positions exprimées dans deux devises, des
frais configurés par banque, des hashes sans IBAN brut et un solde global nul.
Une commande invalide affiche l'usage sans lire de fichier.

## Stratégie de preuve

Les tests suivent les cinq jours :

1. audit de pureté, reporter isolé et absence de temps système dans le cœur;
2. netting immutable et mille exécutions identiques;
3. nettoyage, `andThen`, `compose`, parsing et anonymisation;
4. currying, limites par type, frais par banque et profils MAD/EUR;
5. pipeline complet, rapports identiques en octets, CLI et démo sans sortie
   pendant l'appel du cœur.

L'acceptation exige la suite S1–S9 sous Java 21 et Java 17, les deux profils de
démonstration, un audit statique du paquet v2.0 et une revue mentor indépendante.
Le jalon final porte la version `2.0.0-SNAPSHOT` et le tag `marc-v2.0`.
