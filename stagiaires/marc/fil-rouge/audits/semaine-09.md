# Audit de pureté — Semaine 9

## Périmètre

Cet audit examine le moteur actif v1.3 avant la création du cœur v2.0. Les
anciens packages v0.x à v1.2 restent des exercices exécutables; leurs points
d'entrée et leurs affichages ne font pas partie du cœur S9.

## Effets observés en v1.3

| Emplacement | Effet | Raison | Frontière v2.0 |
|---|---|---|---|
| `ClearingAppV13.runFile` | Lecture de fichier et `println` | Charger et présenter le batch | `IOBridge` et `ClearingReporter` |
| `runClearingAppV13` | `Console.err` | Afficher l'usage CLI | Point d'entrée uniquement |
| `ClearingAppV13.runDemo` | Client HTTP et serveur local | Démontrer l'interopérabilité S8 | Absent du cœur v2.0 |
| Paramètres par défaut v1.3 | `Clock.systemUTC` et `UUID.randomUUID` | Dater et identifier un batch connecté | Configuration fournie ou absente en v2.0 |
| `HttpExchangeRateService` | Requête réseau | Charger les taux S8 | `Map` de taux immutable injectée |
| `LocalExchangeRateServer` | Socket HTTP | Fournir une démo reproductible | Absent du cœur v2.0 |
| `SecureBatchFactory` | Lecture d'un `Clock` injecté | Produire le temps bancaire | Pas de temps dans le rapport logic-only |
| `JavaCollectionAdapters.liveView` | Vue mutable Java | Montrer la sémantique d'interopérabilité | Absent du cœur v2.0 |
| `BankRepository` | `ArrayList` interne | Exercice Java/Spring avec copie défensive | `Set` immutable dans la configuration |

## Calculs déjà purs

- `AdvancedTransactionValidator` reçoit une transaction et retourne erreurs et
  avertissements sans I/O.
- `CurrencyConversion` reçoit un provider; son calcul est déterministe pour un
  provider déterministe, mais l'implémentation HTTP reste un effet externe.
- `BilateralNetting` et `MultilateralNetting` transforment des collections
  immutables avec `groupBy`, `foldLeft` et `updatedWith`.
- `SecurityUtils.hashIban` crée un hash SHA-256 déterministe sans état partagé.
- Les case classes et enums de `clearing.model` ne modifient aucun état et
  n'affichent rien.

## Règle S9

Les fichiers de calcul de `clearing.v20` n'importent ni fichier, ni console, ni
HTTP, ni Spring, ni temps, ni UUID. Ils n'utilisent pas `var`, `throw` ou une
collection mutable. Ils reçoivent toutes leurs données et retournent des types
immuables. Seuls `IOBridge`, `ClearingReporter` et le point d'entrée peuvent
effectuer une action observable.

## Commande d'audit final

```bash
rg -n 'println|Console|Source|HttpClient|HttpServer|springframework|Clock|UUID|\bvar\b|\bthrow\b|collection\.mutable' \
  src/main/scala/clearing/v20
```

Chaque résultat devra appartenir à `IOBridge.scala` ou au point d'entrée. Le
rapport d'acceptation consignera les éventuelles exceptions justifiées.
