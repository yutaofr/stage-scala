# Starter Kit Semaine 14 : observer ZIO dans le fil rouge

La semaine 14 ne remplace pas le moteur construit de S1 à S13. Elle ajoute un seul petit module d'observation dans `distributed.zio` pour voir ce que ZIO apporte autour du cœur Scala déjà stabilisé.

## Contrat pédagogique

- Le stagiaire continue le **même Clearing Engine**.
- Le domaine `clearing.model` reste intact.
- Le cœur `clearing.core` reste en Scala de base : `Either`, validation pure, netting pur.
- Le nouveau code ZIO se limite à `src/main/scala/distributed/zio/ZioClearingModule.scala`.
- Le fichier contient un seul objet Scala : `ZioClearingModule`.
- Chaque TP observe une partie du même module et dure 5 à 10 minutes.
- Aucun exercice ne demande de créer une nouvelle architecture, un vrai client HTTP, un vrai repository, ou un circuit breaker complet.

## Lien avec les semaines précédentes

| Déjà construit | Ce que S14 observe avec ZIO |
|---|---|
| `TransactionId`, `BankCode`, `Money` | ZIO importe ces types, il ne les recrée pas |
| `TransactionValidator` | `ValidationService` enveloppe cette validation pure |
| `PureNettingCalculator` | `NettingService` enveloppe ce calcul pur |
| `ClearingError` | ZIO garde les erreurs métier dans le canal `E` |
| Concurrence S13 | `foreachPar`, `withParallelism` et `timeout` rendent le parallèle local et observable |

## Préparation

```bash
cd fil-rouge
sbt test
```

Le projet doit compiler avant d'ajouter le module. Si cette commande échoue, on corrige le fil rouge avant de parler de ZIO.

## Module unique à copier

**Fichier à créer :** `fil-rouge/src/main/scala/distributed/zio/ZioClearingModule.scala`

```scala
package distributed.zio

import clearing.core.*
import clearing.model.*
import zio.*

import java.io.{BufferedReader, StringReader}

/**
 * Petit module ZIO ajouté au moteur fil rouge.
 *
 * Il ne change pas le domaine et ne remplace pas le cœur Scala de base.
 * Son rôle est d'observer comment ZIO décrit :
 *   - les dépendances avec R;
 *   - les erreurs attendues avec E;
 *   - la valeur produite avec A;
 *   - les ressources, le parallèle et le retry.
 */
object ZioClearingModule extends ZIOAppDefault:

  // Données du langage métier construit avant S14.
  // On utilise les types opaques existants pour éviter un deuxième mini-domaine.
  private val awb = BankCode.unsafe("AWB")
  private val cih = BankCode.unsafe("CIH")

  val knownBanks: Set[BankCode] = Set(awb, cih)

  val sampleBatch: List[Transaction] =
    List(
      Transaction(TransactionId.unsafe("tx-zio-1"), awb, cih, Money(BigDecimal("100.00"))),
      Transaction(TransactionId.unsafe("tx-zio-2"), cih, awb, Money(BigDecimal("40.00")))
    )

  /**
   * Version Scala de base.
   *
   * Intérêt pédagogique :
   *   - montrer que le cœur existant fonctionne déjà;
   *   - garder un point de comparaison clair avec ZIO;
   *   - rappeler que ZIO n'est pas nécessaire pour le calcul pur.
   */
  def baseScalaNetting(txs: List[Transaction]): Either[ClearingError, Map[BankCode, Money]] =
    val validator = TransactionValidator(knownBanks)
    val validated: Either[ClearingError, List[Transaction]] =
      txs.foldRight(Right(Nil): Either[ClearingError, List[Transaction]]) { (tx, acc) =>
        for
          valid <- validator.validate(tx)
          rest  <- acc
        yield valid :: rest
      }

    validated.map(PureNettingCalculator.calculateNetPositions)

  /**
   * Version ZIO du même enchaînement.
   *
   * À lire comme : ce programme a besoin de ValidationService et NettingService,
   * peut échouer avec ClearingError, et produit une Map de positions nettes.
   */
  val validateAndNetting: ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]] =
    for
      validator <- ZIO.service[ValidationService]
      netting   <- ZIO.service[NettingService]
      valid     <- ZIO.foreach(sampleBatch)(validator.validate)
      positions <- netting.calculate(valid)
    yield positions

  def report(positions: Map[BankCode, Money]): String =
    positions.toList
      .sortBy { case (bank, _) => bank.value }
      .map { case (bank, amount) => s"${bank.value}: ${amount.format}" }
      .mkString("\n")

  final case class Batch(name: String, delay: Duration)

  private val batches: List[Batch] =
    List(
      Batch("validation-A", 400.millis),
      Batch("validation-B", 400.millis),
      Batch("validation-C", 400.millis),
      Batch("validation-D", 400.millis)
    )

  // Simulation courte d'une validation distante.
  // ZIO.sleep ne bloque pas un thread pendant l'attente.
  private def process(batch: Batch): UIO[String] =
    ZIO.sleep(batch.delay) *> ZIO.succeed(s"${batch.name} ok")

  private def measure[A](label: String)(effect: UIO[A]): UIO[String] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
    yield s"$label: $value en ${(end - start) / 1000000} ms"

  /**
   * Scala Future démarre souvent des calculs dès leur création.
   * Ici, foreachPar reste une description; withParallelism(2) rend la borne locale.
   */
  val parallelPreview: UIO[String] =
    for
      sequential <- measure("séquentiel")(ZIO.foreach(batches)(process))
      parallel   <- measure("parallèle x2")(ZIO.foreachPar(batches)(process).withParallelism(2))
    yield s"$sequential\n$parallel"

  val timeoutPreview: UIO[Option[String]] =
    process(Batch("validation-lente", 3.seconds)).timeout(1.second)

  private val auditCsv =
    """tx-zio-1,AWB,CIH,100.00
      |tx-zio-2,CIH,AWB,40.00
      |""".stripMargin

  /**
   * Scala de base utiliserait try/finally autour d'un BufferedReader.
   * ZIO.acquireRelease garde l'acquisition et la libération dans la description.
   */
  private def openAuditReader: ZIO[Scope, Nothing, BufferedReader] =
    ZIO.acquireRelease(
      Console.printLine("audit acquire").orDie.as(new BufferedReader(new StringReader(auditCsv)))
    ) { reader =>
      ZIO.attempt(reader.close()).orDie *> Console.printLine("audit release").orDie
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
        Console.printLine(s"audit read: $line").orDie *>
          ZIO.fail(s"ligne audit rejetée: $line")
      case line =>
        Console.printLine(s"audit read: $line").orDie *>
          readLoop(reader, line :: acc, failAfterFirst)
    }

  def readAuditLines(failAfterFirst: Boolean): IO[String, List[String]] =
    ZIO.scoped {
      for
        reader <- openAuditReader
        lines  <- readLoop(reader, Nil, failAfterFirst)
      yield lines
    }

  /**
   * Retry lié au fil rouge : on retente seulement une panne d'infrastructure.
   * Une erreur métier comme UNKNOWN_BANK ne doit pas être retentée.
   */
  val infrastructureOnly: Schedule[Any, ClearingError, Any] =
    (Schedule.exponential(100.millis) && Schedule.recurs(3)).whileInput {
      case ClearingError.InfrastructureFailure(_) => true
      case _                                      => false
    }

  def publishPositions(counter: Ref[Int], positions: Map[BankCode, Money]): IO[ClearingError, String] =
    for
      attempt <- counter.updateAndGet(_ + 1)
      _       <- Console.printLine(s"publication clearing #$attempt").orDie
      receipt <-
        if attempt < 3 then ZIO.fail(ClearingError.InfrastructureFailure("journal indisponible"))
        else ZIO.succeed(s"publication OK (${positions.size} positions)")
    yield receipt

  val unknownBankPreview: UIO[Either[ClearingError, Map[BankCode, Money]]] =
    validateAndNetting
      .tapError(error => Console.printLine(s"erreur métier: ${error.code}").orDie)
      .retry(infrastructureOnly)
      .provide(ValidationService.live(Set(awb)) ++ NettingService.live)
      .either

  def run =
    // Les couches branchent les services ZIO existants sur le cœur Scala.
    // Le cœur n'est pas modifié; on ajoute seulement un adaptateur observable.
    val services = ValidationService.live(knownBanks) ++ NettingService.live

    val demo: ZIO[ValidationService & NettingService, ClearingError, Unit] =
      for
        baseResult <- ZIO.fromEither(baseScalaNetting(sampleBatch))
        _          <- Console.printLine("Scala base:\n" + report(baseResult)).orDie
        positions  <- validateAndNetting
        _          <- Console.printLine("ZIO module:\n" + report(positions)).orDie
        preview    <- parallelPreview
        _          <- Console.printLine(preview).orDie
        auditLines <- readAuditLines(failAfterFirst = false).mapError(ClearingError.InfrastructureFailure.apply)
        _          <- Console.printLine(s"audit lines: ${auditLines.size}").orDie
        timeout    <- timeoutPreview
        _          <- Console.printLine(s"timeout: $timeout").orDie
        business   <- unknownBankPreview
        _          <- Console.printLine(s"retry UNKNOWN_BANK: $business").orDie
        counter    <- Ref.make(0)
        published  <- publishPositions(counter, positions).retry(infrastructureOnly).either
        _          <- Console.printLine(s"retry publication: $published").orDie
      yield ()

    demo
      .foldZIO(
        error => Console.printLine(s"Erreur contrôlée: ${error.code} - ${error.message}").orDie,
        _ => ZIO.unit
      )
      .provide(services)
```

## Sortie attendue

```text
Scala base:
AWB: -60.00 DH
CIH: 60.00 DH
ZIO module:
AWB: -60.00 DH
CIH: 60.00 DH
séquentiel: ...
parallèle x2: ...
audit acquire
audit read: tx-zio-1,AWB,CIH,100.00
audit read: tx-zio-2,CIH,AWB,40.00
audit release
audit lines: 2
timeout: None
erreur métier: UNKNOWN_BANK
retry UNKNOWN_BANK: Left(UnknownBank(CIH))
publication clearing #1
publication clearing #2
publication clearing #3
retry publication: Right(publication OK (2 positions))
```

## Lecture par jour

### Jour 1 - Effet et comparaison Scala de base

Observe `baseScalaNetting` puis `validateAndNetting`.

```scala
def baseScalaNetting(...): Either[ClearingError, Map[BankCode, Money]]
val validateAndNetting: ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]]
```

La logique métier reste la même. ZIO ajoute la description d'exécution et le canal des dépendances.

### Jour 2 - Services et ZLayer

Observe :

```scala
val services = ValidationService.live(knownBanks) ++ NettingService.live
...
.provide(services)
```

Le programme déclare ses besoins dans `R`; les couches sont fournies au bord du programme.

### Jour 3 - Ressource et Scope

Observe `openAuditReader` et `readAuditLines`.

Le code équivaut à un `try/finally`, mais la fermeture reste garantie dans la composition ZIO.

### Jour 4 - Parallèle borné et timeout

Observe `parallelPreview` et `timeoutPreview`.

Le stagiaire change seulement `withParallelism(2)` ou `timeout(1.second)`. Il n'ajoute pas de pool, pas de thread manuel.

### Jour 5 - Retry borné

Observe `infrastructureOnly`, `unknownBankPreview` et `publishPositions`.

La publication est retentée car elle échoue avec `InfrastructureFailure`. L'erreur `UNKNOWN_BANK` n'est pas retentée, car c'est une erreur métier définitive.

## Rappel tuteur

Si un stagiaire commence à modifier `clearing.core`, `clearing.model`, ou à créer un vrai client HTTP, il sort du cadre de la semaine 14. Le bon recadrage est : "on observe ZIO autour du moteur existant; on ne migre pas encore le moteur".
