# Starter Kit Semaine 14 : ZIO — Orchestration des Effets

Le kit part du projet compilable [`../../fil-rouge`](../../fil-rouge). Le stagiaire implémente les zones marquées `TODO` et conserve les erreurs métier dans le canal typé.

## Utilisation

1. Copie le dossier `fil-rouge/` dans ton espace de travail.
2. Lance `sbt test` avant toute modification.
3. Copie ensuite chaque kit dans le chemin indiqué.
4. Compile après chaque exercice.
5. Remplace chaque `???` par une implémentation ; aucun `???` ne doit rester dans le livrable.

## Kit 14.0 — Contrat de reprise S13 → S14

**Projet fourni :** `fil-rouge/`

Le projet contient :

- `clearing.model` : `TransactionId`, `BankCode`, `Money` et `Transaction` ;
- `clearing.core` : `ClearingError`, le validateur et le netting pur ;
- `clearing.contract` : `TransactionSubmittedV1` et son codec JSON ;
- les propriétés minimales héritées de S12 ;
- un `build.sbt` exécutable.

**Contrôle obligatoire :**

```bash
cd fil-rouge
sbt test
```

Ne recrée pas ces types dans `distributed.*`. Les couches distribuées importent le domaine, puis convertissent les types opaques en primitives uniquement aux frontières.

---

## Kit 14.1 — Premier Programme ZIO

**Fichier fourni :** `src/main/scala/distributed/zio/ZioStarter.scala`

```scala
package distributed.zio

import zio.*
import clearing.core.ClearingError
import clearing.model.*
import zio.Console.*
import java.io.IOException

/**
 * STARTER KIT : Le runner ZIO est pré-câblé.
 * Le stagiaire écrit la logique dans "clearingLogic".
 */
object ZioStarter extends ZIOAppDefault:

  // === ZONE STAGIAIRE ===
  enum InputError:
    case EmptyBank
    case InvalidCount(value: String)

  val clearingLogic: ZIO[Any, IOException | InputError, Unit] = for
    _ <- printLine("=== Moteur de Clearing ZIO ===")
    // TODO : Demander le nombre de transactions à traiter via readLine
    // TODO : Générer N transactions aléatoires
    // TODO : Afficher le résumé
    _ <- printLine("Fin du traitement")
  yield ()

  def run = clearingLogic
```

---

## Kit 14.2 — ZLayer Architecture Template

**Fichier fourni :** `src/main/scala/distributed/zio/ClearingLayers.scala`

```scala
package distributed.zio

import zio.*

/**
 * STARTER KIT : L'architecture en couches est pré-définie.
 * Le stagiaire implémente les "live" de chaque service.
 */

// --- Service Interfaces (fournis par le tuteur) ---

trait TransactionRepository:
  def findAll: Task[List[Transaction]]
  def save(tx: Transaction): Task[Unit]

trait ValidationService:
  def validate(tx: Transaction): IO[ClearingError, Transaction]

trait NettingService:
  def calculate(txs: List[Transaction]): UIO[Map[BankCode, Money]]

trait ReportService:
  def generate(positions: Map[BankCode, Money]): UIO[String]

// --- Companion Objects avec ZLayer (fournis par le tuteur) ---

object TransactionRepository:
  val live: ZLayer[Any, Nothing, TransactionRepository] = ZLayer.succeed {
    new TransactionRepository:
      // === ZONE STAGIAIRE : implémenter ===
      def findAll: Task[List[Transaction]] = ???
      def save(tx: Transaction): Task[Unit] = ???
  }

  // Version Mock pour les tests
  val mock: ZLayer[Any, Nothing, TransactionRepository] = ZLayer.succeed {
    new TransactionRepository:
      def findAll = ZIO.succeed(List.empty)
      def save(tx: Transaction) = ZIO.unit
  }

object ValidationService:
  // === ZONE STAGIAIRE : créer le ZLayer.succeed avec l'implémentation ===
  val live: ZLayer[Any, Nothing, ValidationService] = ???

object NettingService:
  // === ZONE STAGIAIRE : créer le ZLayer ===
  val live: ZLayer[Any, Nothing, NettingService] = ???

object ReportService:
  // === ZONE STAGIAIRE : créer le ZLayer ===
  val live: ZLayer[Any, Nothing, ReportService] = ???

// --- Programme Principal (fourni par le tuteur) ---

object ClearingApp extends ZIOAppDefault:
  val program = for
    repo    <- ZIO.service[TransactionRepository]
    txs     <- repo.findAll
    valSvc  <- ZIO.service[ValidationService]
    valid   <- ZIO.foreach(txs)(valSvc.validate)
    netting <- ZIO.service[NettingService].flatMap(_.calculate(valid))
    report  <- ZIO.service[ReportService].flatMap(_.generate(netting))
    _       <- Console.printLine(report)
  yield ()

  def run = program.provide(
    TransactionRepository.live,
    ValidationService.live,
    NettingService.live,
    ReportService.live
  )
```

**Exercice du stagiaire :** utiliser `ZIO.fromEither` dans `ValidationService.live`, puis déléguer le netting au cœur pur. Lancer avec `mock`, puis avec `live`.

---

## Kit 14.3 — Resource Management

```scala
package distributed.zio

import zio.*
import clearing.model.Transaction
import java.io.{BufferedReader, FileReader}

/**
 * STARTER KIT : La gestion de ressource est pré-câblée.
 * Le stagiaire implémente le parsing des lignes.
 */
object FileProcessor:
  def readTransactionFile(path: String): ZIO[Any, Throwable, List[Transaction]] =
    ZIO.acquireReleaseWith(
      acquire = ZIO.attempt(new BufferedReader(new FileReader(path)))
    )(
      release = reader => ZIO.attempt(reader.close()).orDie
    ) { reader =>
      ZIO.attempt {
        // === ZONE STAGIAIRE ===
        // TODO : Lire toutes les lignes et les parser en Transaction
        // Réutiliser Transaction.fromCsv du Mois 2
        ???
      }
    }
```

---

## Kit 14.4 — Fiber Parallel Template

```scala
package distributed.zio

import zio.*
import clearing.model.*

/**
 * STARTER KIT : Le pattern de parallélisme est fourni.
 * Le stagiaire injecte sa logique de netting.
 */
object ParallelClearing:
  def clearBatch(
    batches: List[List[Transaction]]
  ): ZIO[NettingService, Nothing, List[Map[BankCode, Money]]] =
    ZIO.foreachPar(batches) { batch =>
      for
        svc    <- ZIO.service[NettingService]
        result <- svc.calculate(batch)
      yield result
    }.withParallelism(4)  // Au plus 4 validations actives

  def clearWithTimeout(
    txs: List[Transaction],
    timeout: Duration = 2.seconds
  ): ZIO[NettingService, Throwable, Map[BankCode, Money]] =
    val batches = txs.grouped(1000).toList
    clearBatch(batches)
      .map(_.foldLeft(Map.empty[BankCode, Money]) { (acc, m) =>
        // === ZONE STAGIAIRE ===
        // TODO : fusionner les maps de positions nettes
        ???
      })
      .timeoutFail(new RuntimeException("Timeout !"))(timeout)
```

---

## Kit 14.5 — Retry & Circuit Breaker

```scala
package distributed.zio

import zio.*

/**
 * STARTER KIT : Les politiques de retry sont pré-configurées.
 * Le stagiaire branche sa logique d'appel externe.
 */
object ResilientCalls:
  // Politique de retry : au plus 3 nouvelles tentatives, délai exponentiel
  val retryPolicy: Schedule[Any, Throwable, Any] =
    (Schedule.exponential(100.millis) && Schedule.recurs(3)).jittered

  def callExchangeRate(currency: String): ZIO[Any, Throwable, BigDecimal] =
    ZIO.attempt {
      // === ZONE STAGIAIRE ===
      // TODO : Appeler le service de taux de change
      // Le mock échouera 3 fois sur 4 pour tester le retry
      ???
    }.retry(retryPolicy)
     .tapError(e => Console.printLine(s"Échec définitif pour $currency : ${e.getMessage}").orDie)
```
