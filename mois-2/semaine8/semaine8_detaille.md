# Semaine 8 : Interopérabilité Java/Spring (5 jours)

## Jour 1 — Import Java depuis Scala
**Cours (2h)** : Transparence JVM. Import de packages Java. Utilisation de `java.time`, `java.util.UUID`, `java.security.MessageDigest`. Gestion des `null` Java en Scala.
**TP (4h)** : Hasher les IBANs avec SHA-256 (via `MessageDigest`). Ajouter un `UUID` unique à chaque batch de clearing. Formater les dates avec `java.time.LocalDateTime`.

## Jour 2 — Collection Converters
**Cours (2h)** : `scala.jdk.CollectionConverters`. Convertir `java.util.List` → `scala.List` et inversement. Wrappers vs copies profondes.
**TP (4h)** : Créer un mock de "Référentiel Bancaire" Java qui retourne des `ArrayList<String>`. L'adapter proprement en Scala. Tester les conversions.

## Jour 3 — Appel de Services REST
**Cours (2h)** : Utilisation de `java.net.http.HttpClient` depuis Scala. Parsing JSON simple. Appels synchrones vs asynchrones.
**TP (4h)** : Appeler un endpoint simulé (mocké localement) de taux de change. Intégrer les taux dans le moteur de conversion de devises.

## Jour 4 — Spring Context & Scala
**Cours (2h)** : Comment Spring Boot peut charger du code Scala. Annotations `@Service`, `@Repository`. Injection via constructeur.
**TP (4h)** : Créer un petit `@Repository` Spring (en Java) qui fournit la liste des banques autorisées. L'appeler depuis le code Scala de validation.

## Jour 5 — Clearing Engine v1.3 & Démo Mois 2
**Cours (2h)** : Bilan du Mois 2. Comment les ADTs + Interop = un code métier solide et intégrable.
**TP (4h)** : Le moteur v1.3 utilise des ADTs exhaustifs, gère les erreurs proprement, calcule le netting N-à-N, et appelle des services Java/Spring pour les données de référence. **Démonstration au tuteur.**
