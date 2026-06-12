# Starter Kit Semaine 14 : ZIO — Orchestration des Effets

Le stagiaire ne doit PAS écrire le boilerplate ZIO. On lui fournit les couches et les runners. Il implémente la logique métier à l'intérieur.

---

## Kit 14.1 — Premier Programme ZIO

**Fichier fourni :** `src/main/scala/distributed/zio/ZioStarter.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*

/**
 * STARTER KIT : Le runner ZIO est pré-câblé.
 * Le stagiaire écrit la logique dans "clearingLogic".
 */
object ZioStarter extends ZIOAppDefault:

  // === ZONE STAGIAIRE ===
  val clearingLogic: ZIO[Any, Nothing, Unit] = for
    _ <- printLine("=== Moteur de Clearing ZIO ===").orDie
    // TODO : Demander le nombre de transactions à traiter via readLine
    // TODO : Générer N transactions aléatoires
    // TODO : Afficher le résumé
    _ <- printLine("Fin du traitement").orDie
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
  def findAll: Task[List[clearing.Transaction]]
  def save(tx: clearing.Transaction): Task[Unit]

trait ValidationService:
  def validate(tx: clearing.Transaction): Task[Either[clearing.ClearingError, clearing.Transaction]]

trait NettingService:
  def calculate(txs: List[clearing.Transaction]): Task[Map[String, BigDecimal]]

trait ReportService:
  def generate(positions: Map[String, BigDecimal]): Task[String]

// --- Companion Objects avec ZLayer (fournis par le tuteur) ---

object TransactionRepository:
  val live: ZLayer[Any, Nothing, TransactionRepository] = ZLayer.succeed {
    new TransactionRepository:
      // === ZONE STAGIAIRE : implémenter ===
      def findAll: Task[List[clearing.Transaction]] = ???
      def save(tx: clearing.Transaction): Task[Unit] = ???
  }

  // Version Mock pour les tests
  val mock: ZLayer[Any, Nothing, TransactionRepository] = ZLayer.succeed {
    new TransactionRepository:
      def findAll = ZIO.succeed(List.empty)
      def save(tx: clearing.Transaction) = ZIO.unit
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
    results <- ZIO.foreach(txs)(valSvc.validate)
    valid    = results.collect { case Right(tx) => tx }
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

**Exercice du stagiaire :** Remplir les `???` dans les `live` de chaque service en réutilisant le code du Mois 3 (v2.3). Lancer avec `Mock` d'abord, puis `live`.

---

## Kit 14.3 — Resource Management

```scala
package distributed.zio

import zio.*
import java.io.{BufferedReader, FileReader}

/**
 * STARTER KIT : La gestion de ressource est pré-câblée.
 * Le stagiaire implémente le parsing des lignes.
 */
object FileProcessor:
  def readTransactionFile(path: String): ZIO[Any, Throwable, List[clearing.Transaction]] =
    ZIO.acquireReleaseWith(
      acquire = ZIO.attempt(new BufferedReader(new FileReader(path)))
    )(
      release = reader => ZIO.succeed(reader.close())
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

/**
 * STARTER KIT : Le pattern de parallélisme est fourni.
 * Le stagiaire injecte sa logique de netting.
 */
object ParallelClearing:
  def clearBatch(
    batches: List[List[clearing.Transaction]]
  ): ZIO[NettingService, Throwable, List[Map[String, BigDecimal]]] =
    ZIO.foreachPar(batches) { batch =>
      for
        svc    <- ZIO.service[NettingService]
        result <- svc.calculate(batch)
      yield result
    }.withParallelism(4)  // Max 4 fibers en parallèle

  def clearWithTimeout(
    txs: List[clearing.Transaction],
    timeout: Duration = 2.seconds
  ): ZIO[NettingService, Throwable, Map[String, BigDecimal]] =
    val batches = txs.grouped(1000).toList
    clearBatch(batches)
      .map(_.foldLeft(Map.empty[String, BigDecimal]) { (acc, m) =>
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
  // Politique de retry : 3 tentatives, délai exponentiel
  val retryPolicy: Schedule[Any, Throwable, Any] =
    Schedule.exponential(100.millis) && Schedule.recurs(3)

  def callExchangeRate(currency: String): ZIO[Any, Throwable, BigDecimal] =
    ZIO.attempt {
      // === ZONE STAGIAIRE ===
      // TODO : Appeler le service de taux de change
      // Le mock échouera 3 fois sur 4 pour tester le retry
      ???
    }.retry(retryPolicy)
     .tapError(e => Console.printLine(s"Échec définitif pour $currency : ${e.getMessage}").orDie)
```
