# Semaine 8 — Interopérabilité Java et Clearing Engine v1.3

## Objectif

Connecter le moteur fonctionnel Scala à des APIs et composants Java sans
dégrader ses invariants : anonymiser les IBAN, dater chaque batch, traverser
des collections Java, obtenir les taux par HTTP, convertir en MAD et assembler
des services annotés Spring.

## Notions à maîtriser

- Appel d'une API Java depuis Scala : `MessageDigest`, `UUID`, `Clock`,
  `ZonedDateTime`, `LocalDateTime` et `DateTimeFormatter`.
- Appel d'une case class Scala depuis une classe Java.
- `ArrayList`, `HashMap`, `asScala`, `asJava`, vue liée et copie immutable.
- Conversion sûre de `Double` avec `BigDecimal.valueOf`.
- `HttpClient`, `HttpRequest`, `HttpResponse`, statut HTTP et petit JSON.
- `Option` à une frontière faillible et isolation d'une devise indisponible.
- Cache local : un seul appel par devise distincte dans un batch.
- `@Repository`, `@Component`, `@Service` et injection constructeur
  `@Autowired`.
- Assemblage manuel d'un graphe d'objets et réutilisation des modules v1.1/v1.2.

## Exercices

- [x] J1 — Produire le SHA-256 UTF-8 d'un IBAN, créer un UUID par batch et
  formater l'heure bancaire marocaine sans journaliser l'IBAN brut.
- [x] J2 — Lire de vraies `ArrayList`/`HashMap` Java, distinguer vue et copie,
  filtrer les codes `TX-`, convertir les soldes et retourner une liste Java.
- [x] J3 — Appeler un endpoint local avec le client HTTP du JDK, lire MAD/EUR/
  USD, mettre les taux en cache et isoler une panne USD.
- [x] J4 — Créer `BankRepository.java`, annoter les composants Spring, injecter
  leurs dépendances par constructeur et les instancier manuellement.
- [x] J5 — Connecter CSV, validation, repository, change, sécurité et netting
  dans v1.3; démontrer le scénario nominal et le scénario réseau dégradé.

## Premier livrable

Une version `v1.3` qui traite `transactions-v13.csv`, convertit les montants en
MAD avant le netting, rejette seulement les transactions dont le taux manque,
crée un batch UUID horodaté, affiche uniquement les hashes d'IBAN et conserve
un solde multilatéral nul.

## Critères de validation

- [x] SHA-256 utilise UTF-8 et rend 64 caractères hexadécimaux minuscules.
- [x] Un batch porte un UUID et un `ZonedDateTime` en `Africa/Casablanca`.
- [x] Le rapport v1.3 ne contient aucun IBAN brut accepté.
- [x] La vue Java/Scala reflète une mutation; la copie immutable reste stable.
- [x] Les collections sont converties dans les deux sens sans perdre l'ordre.
- [x] Les éléments, clés et valeurs `null` venus de Java sont traités avec
  `Option` sans provoquer de NPE.
- [x] Le client HTTP traite 200, 404, JSON incomplet, URL invalide et serveur
  inaccessible sans lever d'exception métier.
- [x] Chaque devise distincte provoque au plus un appel par batch.
- [x] Un taux nul ou négatif est isolé comme un taux invalide et n'entre jamais
  dans le netting.
- [x] Une absence de taux USD laisse les transactions MAD et EUR avancer.
- [x] Les annotations Spring et `@Autowired` sont visibles par réflexion.
- [x] Le repository Java retourne une copie défensive et crée les `Bank` Scala.
- [x] Le pipeline réutilise la validation v1.1 et le netting v1.2.
- [x] Les rejets v1.1, repository et FX restent séparés et expliqués.
- [x] Le netting nominal et dégradé conserve une somme globale nulle.
- [x] Les tests v1.2 et v1.3 passent ensemble sous Java 21.
- [x] Tous les tests S1 à S8 passent sous Java 21 et Java 17 Docker.
- [x] Les deux démonstrations v1.3 passent sous Java 21 et Java 17 Docker.
- [x] Le code v1.3 passe l'audit fonctionnel et la revue mentor.

## Journal d'exécution

### Cycles TDD observés

- J1 a commencé par huit erreurs de compilation. Cinq tests vérifient ensuite
  deux vecteurs SHA-256, le temps marocain, l'UUID injecté et les UUID réels.
- J2 a commencé par six APIs absentes. Six tests prouvent la collection Java
  réelle, la vue, la copie, le filtre, la décimale stable et le retour Java.
- J3 a commencé par huit APIs absentes. Huit tests couvrent le serveur local,
  les erreurs HTTP, l'URL, le cache par devise et le rejet USD ciblé.
- J4 a commencé par onze types ou services absents. Cinq tests couvrent les
  annotations, l'injection, la copie défensive et le pipeline de services.
- J5 a commencé par douze symboles absents. Six tests couvrent les formes CLI,
  le fichier complet, le scénario USD, le chemin illisible et la confidentialité
  du rapport.
- La première revue mentor a reproduit deux frontières insuffisantes : taux
  non positif accepté et `null` Java causant un NPE. Quatre nouveaux tests ont
  d'abord échoué; les taux sont maintenant filtrés dans HTTP et conversion,
  tandis que les adapters utilisent `Option` avant la logique Scala.

### Décisions mentor à confirmer

Spring Framework 6.2.19 apporte les annotations et l'injection nécessaires
sans démarrer Spring Boot. Java 17 reste le niveau minimal commun aux deux
environnements du stage. La démo assemble donc les composants manuellement.

Le serveur de taux écoute seulement sur loopback et un port éphémère. Il rend
la preuve HTTP reproductible et indépendante d'Internet. Le mode
`--network-failure` masque le taux USD, ce qui conserve une preuve déterministe
de dégradation partielle.

Le cœur du netting reçoit uniquement des montants MAD. Lorsqu'un taux manque,
le moteur exclut les IDs concernés avant les calculs; il ne mélange jamais des
devises sous une même unité.

## Validation mentor

**Décision : acceptée.** Le 14/07/2026, la suite complète compile 57 sources
Scala et deux sources Java de production, puis 44 sources de test. Elle exécute
300 tests dans 44 suites, sans échec, sous Java 21 local et Java 17 Docker.

Dans les deux environnements, la démonstration nominale valide cinq
transactions, charge MAD/EUR/USD et termine avec un solde global de 0 MAD. Le
mode dégradé conserve quatre transactions, signale seulement l'USD de l'ID 3
et termine lui aussi à 0 MAD. Les rapports observés contiennent les hashes
SHA-256, jamais les IBAN bruts.

La première revue mentor a signalé un taux non positif encore accepté et des
`null` Java non traités. Les tests ont reproduit les quatre échecs avant
correction. La seconde revue confirme leur fermeture et ne conserve aucun
point critique, important ou mineur. `git diff --check` et l'audit des
anti-patterns Scala sont propres. Le jalon peut recevoir le tag `marc-v1.3`.
