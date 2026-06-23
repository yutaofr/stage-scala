# Starter Kit Semaine 14 : ZIO en observation

Ce kit sert à observer ZIO sans perdre le stagiaire dans une reconstruction du moteur. Chaque snippet est presque complet. Le stagiaire modifie une petite ligne, relance, puis explique ce qu'il voit.

## Règle de la semaine

- Un exercice dure 5 à 10 minutes.
- Aucun exercice ne demande de recréer le domaine `clearing.*`.
- Aucun snippet ne laisse de trou d'implémentation.
- Une validation visible termine chaque exercice : sortie console, erreur typée, erreur de compilation, ou mesure de temps.
- Si un exercice bloque plus de 10 minutes, le tuteur montre la ligne clé et passe à l'observation.

## Utilisation

1. Pars du projet compilable [`../../fil-rouge`](../../fil-rouge).
2. Lance `sbt test` une fois avant la semaine.
3. Copie le kit du jour dans le chemin indiqué.
4. Lance `sbt "runMain distributed.zio.NomDuKit"` quand le kit déclare un objet `ZIOAppDefault`.
5. Note une phrase de constat après chaque micro-exercice.

## Kit 14.0 - Contrat de reprise S13 vers S14

**Projet fourni :** `fil-rouge/`

Le projet contient déjà :

- `clearing.model` : `TransactionId`, `BankCode`, `Money`, `Transaction`;
- `clearing.core` : `ClearingError`, `TransactionValidator`, `PureNettingCalculator`;
- `distributed.zio` : `ValidationService` et `NettingService`;
- un `build.sbt` avec ZIO.

**Contrôle obligatoire :**

```bash
cd fil-rouge
sbt test
```

Le stagiaire importe ces types. Il ne les recopie pas dans `distributed.*`.

---

## Kit 14.1 - Premier programme ZIO

**Fichier à créer :** `src/main/scala/distributed/zio/ZioStarter.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*
import java.io.IOException

object ZioStarter extends ZIOAppDefault:

  enum InputError:
    case EmptyBank
    case InvalidCount(value: String)

  final case class BatchRequest(bank: String, count: Int)

  private val rawBank = "AWB"
  private val rawCount = "2"

  private def normalizeBank(raw: String): IO[InputError, String] =
    val bank = raw.trim.toUpperCase
    ZIO.cond(bank.nonEmpty, bank, InputError.EmptyBank)

  private def parseCount(raw: String): IO[InputError, Int] =
    ZIO
      .fromOption(raw.trim.toIntOption)
      .orElseFail(InputError.InvalidCount(raw))
      .flatMap { count =>
        ZIO.cond(count >= 0 && count <= 1000, count, InputError.InvalidCount(raw))
      }

  val askRequest: IO[InputError, BatchRequest] =
    for
      bank     <- normalizeBank(rawBank)
      count    <- parseCount(rawCount)
    yield BatchRequest(bank, count)

  private val batchA: UIO[String] =
    ZIO.sleep(300.millis) *> ZIO.succeed("batch-A")

  private val batchB: UIO[String] =
    ZIO.sleep(300.millis) *> ZIO.succeed("batch-B")

  private def timed[A](label: String)(effect: UIO[A]): UIO[String] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
    yield s"$label : $value en ${(end - start) / 1000000} ms"

  val previewParallelism: UIO[String] =
    for
      sequential <- timed("zip")(batchA.zip(batchB).map { case (a, b) => s"$a + $b" })
      parallel   <- timed("zipPar")(batchA.zipPar(batchB).map { case (a, b) => s"$a + $b" })
    yield s"$sequential\n$parallel"

  val clearingLogic: ZIO[Any, IOException | InputError, Unit] =
    for
      _       <- printLine("=== Observation ZIO : console et erreurs typées ===")
      request <- askRequest
      preview <- previewParallelism
      _       <- printLine(s"Demande acceptée : ${request.bank}, ${request.count} transaction(s)")
      _       <- printLine(s"Observation zipPar : $preview")
    yield ()

  def run =
    clearingLogic.catchAll {
      case InputError.EmptyBank =>
        printLine("Erreur contrôlée : la banque est vide").orDie
      case InputError.InvalidCount(value) =>
        printLine(s"Erreur contrôlée : nombre invalide [$value]").orDie
      case error: IOException =>
        printLine(s"Erreur console : ${error.getMessage}").orDie
    }
```

**À observer :**

- `askRequest` est une description. Le runtime l'exécute seulement dans `run`.
- Le type `IOException | InputError` annonce les échecs attendus.
- La sortie compare `zip` et `zipPar` pour rendre le parallèle visible.
- Pour tester les erreurs, le stagiaire change seulement `rawBank` ou `rawCount`.

---

## Kit 14.2 - ZLayer sans construction lourde

**Fichier à créer :** `src/main/scala/distributed/zio/ZLayerObservation.scala`

```scala
package distributed.zio

import clearing.core.*
import clearing.model.*
import zio.*

object ZLayerObservation extends ZIOAppDefault:

  private val awb = BankCode.unsafe("AWB")
  private val cih = BankCode.unsafe("CIH")

  private val sampleTransactions = List(
    Transaction(TransactionId.unsafe("tx-1"), awb, cih, Money(BigDecimal("100.00"))),
    Transaction(TransactionId.unsafe("tx-2"), cih, awb, Money(BigDecimal("40.00")))
  )

  private val knownBanks = Set(awb, cih)

  val program: ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]] =
    for
      validator <- ZIO.service[ValidationService]
      netting   <- ZIO.service[NettingService]
      valid     <- ZIO.foreach(sampleTransactions)(validator.validate)
      positions <- netting.calculate(valid)
    yield positions

  private def show(positions: Map[BankCode, Money]): String =
    positions.toList
      .sortBy { case (bank, _) => bank.value }
      .map { case (bank, amount) => s"${bank.value}: ${amount.format}" }
      .mkString("\n")

  def run =
    program
      .foldZIO(
        error => Console.printLine(s"Erreur métier : ${error.code} - ${error.message}").orDie,
        positions => Console.printLine(show(positions)).orDie
      )
      .provide(
        ValidationService.live(knownBanks),
        NettingService.live
      )
```

**À observer :**

- `program` demande `ValidationService & NettingService`.
- `provide` fournit les couches à la fin.
- Retirer `cih` de `knownBanks` affiche une erreur métier courte.
- Retirer une couche produit une erreur de compilation utile.

---

## Kit 14.3 - Ressource fermée par `Scope`

**Fichier à créer :** `src/main/scala/distributed/zio/ResourceObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*
import java.io.{BufferedReader, StringReader}

object ResourceObservation extends ZIOAppDefault:

  private val csv =
    """tx-1,AWB,CIH,100
      |tx-2,CIH,AWB,40
      |tx-3,AWB,CIH,10
      |""".stripMargin

  private def openReader: ZIO[Scope, Nothing, BufferedReader] =
    ZIO.acquireRelease(
      printLine("acquire reader").orDie.as(new BufferedReader(new StringReader(csv)))
    ) { reader =>
      ZIO.attempt(reader.close()).orDie *> printLine("release reader").orDie
    }

  private def readLoop(
      reader: BufferedReader,
      acc: List[String],
      failAfterFirst: Boolean
  ): IO[String, List[String]] =
    ZIO.attempt(reader.readLine()).mapError(_.getMessage).flatMap {
      case null =>
        ZIO.succeed(acc.reverse)
      case line if failAfterFirst && acc.nonEmpty =>
        printLine(s"read : $line").orDie *> ZIO.fail(s"ligne rejetée : $line")
      case line =>
        printLine(s"read : $line").orDie *> readLoop(reader, line :: acc, failAfterFirst)
    }

  def readAll(failAfterFirst: Boolean): IO[String, List[String]] =
    ZIO.scoped {
      for
        reader <- openReader
        lines  <- readLoop(reader, Nil, failAfterFirst)
      yield lines
    }

  def run =
    readAll(failAfterFirst = false).foldZIO(
      error => printLine(s"erreur observée : $error").orDie,
      lines => printLine(s"${lines.size} ligne(s) lue(s)").orDie
    )
```

**À observer :**

- `acquire reader` apparaît avant la lecture.
- `release reader` apparaît en succès et en échec.
- Le stagiaire ne manipule pas encore de gros fichier CSV.

---

## Kit 14.4 - Concurrence bornée et timeout

**Fichier à créer :** `src/main/scala/distributed/zio/ParallelObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*

object ParallelObservation extends ZIOAppDefault:

  final case class Batch(name: String, delay: Duration)

  private val batches = List(
    Batch("A", 500.millis),
    Batch("B", 500.millis),
    Batch("C", 500.millis),
    Batch("D", 500.millis)
  )

  private def process(batch: Batch): UIO[String] =
    ZIO.sleep(batch.delay) *> ZIO.succeed(s"${batch.name} ok")

  private def measure[A](label: String)(effect: UIO[A]): UIO[A] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
      _     <- printLine(s"$label : ${(end - start) / 1000000} ms").orDie
    yield value

  def run =
    for
      _       <- measure("séquentiel")(ZIO.foreach(batches)(process))
      _       <- measure("parallèle x2")(ZIO.foreachPar(batches)(process).withParallelism(2))
      timeout <- process(Batch("lent", 3.seconds)).timeout(1.second)
      _       <- printLine(s"timeout : $timeout").orDie
    yield ()
```

**À observer :**

- Le parallèle x2 traite quatre batchs en deux vagues.
- `timeout` retourne `None` quand l'effet ne finit pas à temps.
- Le stagiaire change seulement `withParallelism` ou la durée du timeout.

---

## Kit 14.5 - Retry borné et appel court-circuité

**Fichier à créer :** `src/main/scala/distributed/zio/RetryObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*

object RetryObservation extends ZIOAppDefault:

  enum RateError:
    case Temporary(message: String)
    case InvalidCurrency(value: String)

  private val temporaryOnly: Schedule[Any, RateError, Any] =
    (Schedule.exponential(100.millis) && Schedule.recurs(3))
      .whileInput {
        case RateError.Temporary(_)    => true
        case RateError.InvalidCurrency(_) => false
      }

  private def flaky(counter: Ref[Int], currency: String): IO[RateError, BigDecimal] =
    if currency.length != 3 then
      ZIO.fail(RateError.InvalidCurrency(currency))
    else
      for
        attempt <- counter.updateAndGet(_ + 1)
        _       <- printLine(s"appel distant #$attempt").orDie
        rate    <-
          if attempt < 4 then ZIO.fail(RateError.Temporary("service indisponible"))
          else ZIO.succeed(BigDecimal("10.50"))
      yield rate

  private def guarded(counter: Ref[Int], circuitOpen: Ref[Boolean]): IO[RateError, BigDecimal] =
    circuitOpen.get.flatMap {
      case true =>
        printLine("circuit ouvert : aucun appel distant").orDie *>
          ZIO.fail(RateError.Temporary("circuit ouvert"))
      case false =>
        flaky(counter, "EUR").tapError {
          case RateError.Temporary(_)     => circuitOpen.set(true)
          case RateError.InvalidCurrency(_) => ZIO.unit
        }
    }

  def run =
    for
      counter <- Ref.make(0)
      ok      <- flaky(counter, "EUR").retry(temporaryOnly).either
      _       <- printLine(s"retry EUR : $ok").orDie
      invalid <- flaky(counter, "EURO").retry(temporaryOnly).either
      _       <- printLine(s"retry EURO : $invalid").orDie
      open    <- Ref.make(true)
      blocked <- guarded(counter, open).either
      _       <- printLine(s"circuit : $blocked").orDie
    yield ()
```

**À observer :**

- `EUR` réussit au quatrième appel.
- `EURO` échoue sans retry, car l'erreur n'est pas temporaire.
- Le circuit ouvert bloque avant l'appel distant.
