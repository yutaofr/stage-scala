# Marc — Semaine 8 v1.3 Design

## Contrat et options étudiées

La semaine 8 connecte le moteur v1.2 à des APIs Java sans déplacer son cœur
fonctionnel. Le nouveau paquet `clearing.v13` réutilise la validation v1.1 et
les calculs v1.2. Il ajoute quatre frontières : sécurité et temps Java,
collections Java, HTTP et composants annotés Spring.

Trois options ont été comparées. Simuler Java avec des objets Scala simplifie
la compilation, mais ne prouve pas l'interopérabilité bidirectionnelle demandée.
Démarrer une application Spring Boot complète prouve l'intégration, mais ajoute
un serveur, une configuration et un cycle de vie absents du TP. L'option
retenue compile de vraies classes Java, charge `spring-context` et annote les
composants. La démo les instancie manuellement, comme le demande le cours. Elle
reste donc rapide et prépare une future application Spring réelle.

Le projet utilise Spring Framework 6.2.19. La documentation officielle classe
encore cette ligne comme stable et Spring 6 exige Java 17 ou plus. Cette version
respecte les deux environnements de validation du stage : Java 17 et Java 21.

## Sécurité, identifiants et temps

`SecurityUtils.hashIban` appelle `java.security.MessageDigest` avec SHA-256 et
UTF-8. Il retourne 64 caractères hexadécimaux. Deux entrées identiques donnent
le même résultat; deux IBAN distincts restent distincts dans les jeux de test.
Le rapport technique v1.3 n'affiche jamais l'IBAN brut.

`SecureBatchFactory` crée un `SecureBatch` avec un `java.util.UUID`, un
`ZonedDateTime` dans la zone `Africa/Casablanca` et un journal sécurisé par
transaction. Chaque entrée porte l'horodatage bancaire
`dd/MM/yyyy HH:mm:ss`. L'implémentation extrait aussi le `LocalDateTime` avant
formatage. Elle satisfait ainsi le TP quotidien, qui demande `ZonedDateTime`,
et le plan hebdomadaire, qui cite `LocalDateTime`.

La factory reçoit un `Clock` et un fournisseur d'UUID. La production utilise
les valeurs Java réelles; les tests injectent des valeurs fixes. Ce seam rend
le rapport déterministe sans masquer les APIs Java apprises.

## Collections Java et repository

`LegacyJavaMock.java` expose une `ArrayList[String]` de codes de transaction et
une `HashMap[String, Double]` de soldes. `JavaCollectionAdapters` montre les
deux comportements de `asScala` : une vue reflète une mutation Java ultérieure,
tandis que `.toList` crée un instantané immutable. L'adapter filtre les codes
`TX-`, convertit les soldes avec `BigDecimal.valueOf` et sait rendre une liste
Scala à Java avec `asJava`.

`BankRepository.java` porte `@Repository`. Il construit les cinq `Bank` Scala
et les retourne dans une nouvelle `ArrayList`. Cette classe prouve aussi qu'un
composant Java peut créer et rendre un ADT Scala. Le validateur Scala convertit
la collection une seule fois à sa frontière, puis travaille avec un `Set`
immutable de codes.

`SpringTransactionValidator` porte `@Component`; il sépare les transactions
autorisées de celles dont une extrémité manque au repository. `ClearingService`
porte `@Service` et reçoit le validateur par un constructeur `@Autowired`. Les
tests vérifient les annotations par réflexion et exécutent le même graphe
d'objets que la démo manuelle.

## HTTP, cache et conversion MAD

`HttpExchangeRateService` utilise `java.net.http.HttpClient`. Il construit une
requête vers `/rates/<currency>`, contrôle le statut 200 et extrait le champ
numérique `rate` d'un petit JSON. Le code n'ajoute aucune bibliothèque JSON :
Circe appartient au mois 3. La factory refuse une URL malformée. Une erreur de
transport, une interruption, un statut non réussi ou un corps invalide donnent
`None`; une interruption restaure le drapeau du thread.

`LocalExchangeRateServer` utilise le serveur HTTP du JDK sur l'interface
loopback et un port éphémère. Il fournit MAD = 1, EUR = 10,80 et USD = 9,90.
Ainsi, les tests et la démo exercent un vrai échange HTTP sans dépendre
d'Internet.

`CurrencyConversion` collecte les devises distinctes avant le calcul. Il
appelle le provider une seule fois par devise, conserve les taux dans une Map
locale et convertit les transactions trouvées en MAD avec deux décimales. Si un
taux manque, il rejette uniquement les transactions de cette devise et décrit
la cause. Le netting ne reçoit donc jamais un mélange de monnaies.

## Pipeline v1.3 et preuves

`ClearingAppV13` suit ce flux :

```text
CSV
  -> validation v1.1
  -> repository Spring Java
  -> cache de taux HTTP
  -> conversion MAD ou rejet ciblé
  -> SecureBatch UUID + horodatages + hashes
  -> netting bilatéral et multilatéral v1.2
  -> rapport v1.3
```

Le fichier `transactions-v13.csv` contient des transactions MAD, EUR et USD,
plus des lignes invalides. La démo nominale appelle le serveur local, convertit
les devises, anonymise les IBAN, calcule les positions et conserve une somme
nulle. Une seconde démo injecte un provider en panne; le moteur continue,
signale le taux absent et exclut seulement les transactions concernées.

Les tests suivent les cinq jours : vecteurs SHA-256, temps et UUID; vues et
copies de collections; serveur HTTP, statuts et cache; annotations et injection;
intégration nominale et panne. L'acceptation finale exige les tests S1 à S8 et
la démo v1.3 sous Java 21 et Java 17, l'absence d'IBAN brut dans le rapport, un
netting nul et une revue mentor indépendante.
