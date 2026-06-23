# Starter Kit Semaine 14 : ZIO en observation

Ce kit sert à observer ZIO sans perdre le stagiaire dans une reconstruction du moteur. Chaque exemple compare d'abord le réflexe Scala de base, puis montre ce que ZIO rend explicite : l'exécution différée, l'erreur typée, la dépendance, la ressource, la concurrence, ou la résilience.

## Règle de la semaine

- Un exercice dure 5 à 10 minutes.
- Aucun exercice ne demande de recréer le domaine `clearing.*`.
- Aucun snippet ne laisse de trou d'implémentation.
- Chaque snippet explique à quoi sert le code, pourquoi il est écrit de cette manière, et ce que ZIO apporte par rapport à Scala de base.
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

### Pourquoi on repart du domaine existant

En Scala de base, il est facile de dupliquer les types dans chaque couche :

```scala
// Mauvais réflexe : la couche distribuée recrée son propre mini-domaine.
final case class DistributedTransaction(id: String, sender: String, receiver: String, amount: BigDecimal)
```

ZIO ne remplace pas le domaine métier. Il l'entoure avec des effets contrôlés. Le bon réflexe est donc :

```scala
// Bon réflexe : les effets ZIO orchestrent le domaine, ils ne le redéfinissent pas.
import clearing.model.Transaction
import clearing.core.ClearingError
```

**Intérêt :** le stagiaire observe ZIO sur un modèle métier stable. Il ne mélange pas deux sujets : apprendre ZIO et redessiner le Clearing Engine.

---

## Kit 14.1 - Premier programme ZIO

**Idée du jour :** un `ZIO[R, E, A]` est une description. Le code décrit un programme qui peut demander un environnement `R`, échouer avec une erreur `E`, et produire une valeur `A`.

### Scala de base : l'effet part tout de suite

```scala
import scala.util.Try

val rawBank = "AWB"
val rawCount = "2"

// Ici, le parsing s'exécute immédiatement.
// L'erreur devient une exception capturée dans Try, mais son type métier disparaît.
val baseResult: Try[(String, Int)] =
  Try((rawBank.trim.toUpperCase, rawCount.trim.toInt))
```

**Limite :** `Try` dit seulement "une exception peut arriver". Il ne dit pas clairement si l'erreur vient d'une banque vide, d'un nombre invalide, ou d'une panne console.

### ZIO : l'erreur attendue devient une partie du type

**Fichier à créer :** `src/main/scala/distributed/zio/ZioStarter.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*
import java.io.IOException

object ZioStarter extends ZIOAppDefault:

  // Cette erreur appartient au mini-programme d'entrée.
  // Elle est plus précise qu'une exception générique.
  enum InputError:
    case EmptyBank
    case InvalidCount(value: String)

  // La donnée validée que le reste du programme peut utiliser.
  final case class BatchRequest(bank: String, count: Int)

  // Valeurs fixes pour garder l'exercice observable en moins de 10 minutes.
  // Le stagiaire les modifie pour provoquer les chemins d'erreur.
  private val rawBank = "AWB"
  private val rawCount = "2"

  // Rôle : nettoyer la banque et refuser une valeur vide.
  // Pourquoi ZIO.cond : on transforme une condition métier en effet typé.
  private def normalizeBank(raw: String): IO[InputError, String] =
    val bank = raw.trim.toUpperCase
    ZIO.cond(bank.nonEmpty, bank, InputError.EmptyBank)

  // Rôle : convertir une chaîne en Int sans lancer d'exception.
  // Pourquoi IO[InputError, Int] : l'appelant voit le type exact de l'échec.
  private def parseCount(raw: String): IO[InputError, Int] =
    ZIO
      .fromOption(raw.trim.toIntOption)
      .orElseFail(InputError.InvalidCount(raw))
      .flatMap { count =>
        ZIO.cond(count >= 0 && count <= 1000, count, InputError.InvalidCount(raw))
      }

  // Rôle : composer deux validations sans les exécuter tout de suite.
  // Intérêt : la valeur askRequest décrit le travail; run décidera de l'exécuter.
  val askRequest: IO[InputError, BatchRequest] =
    for
      bank  <- normalizeBank(rawBank)
      count <- parseCount(rawCount)
    yield BatchRequest(bank, count)

  // Ces deux effets simulent deux petits traitements indépendants.
  // Ils ne démarrent pas quand on les déclare.
  private val batchA: UIO[String] =
    ZIO.sleep(300.millis) *> ZIO.succeed("batch-A")

  private val batchB: UIO[String] =
    ZIO.sleep(300.millis) *> ZIO.succeed("batch-B")

  // Rôle : mesurer un effet sans sortir du monde ZIO.
  // Pourquoi Clock.nanoTime : la mesure devient elle aussi un effet contrôlé.
  private def timed[A](label: String)(effect: UIO[A]): UIO[String] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
    yield s"$label : $value en ${(end - start) / 1000000} ms"

  // Scala de base ferait souvent deux appels l'un après l'autre.
  // Ici, zip montre la version séquentielle; zipPar montre la version parallèle.
  val previewParallelism: UIO[String] =
    for
      sequential <- timed("zip")(batchA.zip(batchB).map { case (a, b) => s"$a + $b" })
      parallel   <- timed("zipPar")(batchA.zipPar(batchB).map { case (a, b) => s"$a + $b" })
    yield s"$sequential\n$parallel"

  // Le type annonce tout ce qui peut arriver :
  // - Any : pas de dépendance externe;
  // - IOException | InputError : erreur console ou erreur métier d'entrée;
  // - Unit : le programme affiche un résultat.
  val clearingLogic: ZIO[Any, IOException | InputError, Unit] =
    for
      _       <- printLine("=== Observation ZIO : console et erreurs typées ===")
      request <- askRequest
      preview <- previewParallelism
      _       <- printLine(s"Demande acceptée : ${request.bank}, ${request.count} transaction(s)")
      _       <- printLine(s"Observation zipPar : $preview")
    yield ()

  // run est la frontière : le runtime ZIO exécute enfin la description.
  // catchAll garde une sortie lisible au lieu d'une stack trace intimidante.
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

- `askRequest` et `clearingLogic` sont des valeurs qui décrivent le programme.
- Le runtime exécute la description seulement dans `run`.
- L'erreur métier est dans le type, pas cachée dans une exception.
- `zipPar` rend le parallélisme visible sans manipuler de `Thread`.

---

## Kit 14.2 - ZLayer sans construction lourde

**Idée du jour :** le canal `R` de `ZIO[R, E, A]` rend les dépendances explicites. Un programme qui demande `ValidationService & NettingService` ne peut pas tourner tant que ces services ne sont pas fournis.

### Scala de base : les dépendances sont souvent câblées à la main

```scala
import clearing.core.*
import clearing.model.*

def runBase(txs: List[Transaction]): Either[ClearingError, Map[BankCode, Money]] =
  val validator = TransactionValidator(Set(BankCode.unsafe("AWB"), BankCode.unsafe("CIH")))
  val validated: Either[ClearingError, List[Transaction]] =
    txs.foldRight(Right(Nil): Either[ClearingError, List[Transaction]]) { (tx, acc) =>
      for
        valid <- validator.validate(tx)
        rest  <- acc
      yield valid :: rest
    }

  validated.map(PureNettingCalculator.calculateNetPositions)
```

**Limite :** le programme crée lui-même ses dépendances. Le test, le mock, ou la substitution deviennent plus difficiles à montrer proprement.

### ZIO : le programme déclare ses besoins

**Fichier à créer :** `src/main/scala/distributed/zio/ZLayerObservation.scala`

```scala
package distributed.zio

import clearing.core.*
import clearing.model.*
import zio.*

object ZLayerObservation extends ZIOAppDefault:

  // Données déterministes : le stagiaire observe le mécanisme ZLayer,
  // pas la génération de transactions.
  private val awb = BankCode.unsafe("AWB")
  private val cih = BankCode.unsafe("CIH")

  private val sampleTransactions = List(
    Transaction(TransactionId.unsafe("tx-1"), awb, cih, Money(BigDecimal("100.00"))),
    Transaction(TransactionId.unsafe("tx-2"), cih, awb, Money(BigDecimal("40.00")))
  )

  // Modifier cette ligne permet d'observer une erreur métier contrôlée.
  private val knownBanks = Set(awb, cih)

  // Rôle : déclarer la logique métier sans construire les services.
  // Pourquoi ZIO[ValidationService & NettingService, ...] :
  // le type dit exactement quelles dépendances doivent être fournies.
  val program: ZIO[ValidationService & NettingService, ClearingError, Map[BankCode, Money]] =
    for
      validator <- ZIO.service[ValidationService]
      netting   <- ZIO.service[NettingService]
      valid     <- ZIO.foreach(sampleTransactions)(validator.validate)
      positions <- netting.calculate(valid)
    yield positions

  // Rôle : garder l'affichage hors du calcul métier.
  private def show(positions: Map[BankCode, Money]): String =
    positions.toList
      .sortBy { case (bank, _) => bank.value }
      .map { case (bank, amount) => s"${bank.value}: ${amount.format}" }
      .mkString("\n")

  // Rôle : fournir les couches au bord du programme.
  // Pourquoi provide ici : la logique reste testable et indépendante du câblage.
  // foldZIO transforme une erreur métier en message court et lisible.
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

- `program` ne crée pas ses services; il les demande.
- `provide` branche les implémentations à la fin.
- Retirer `cih` de `knownBanks` montre une erreur métier courte.
- Retirer une couche montre une erreur de compilation : la dépendance manquante est visible.

---

## Kit 14.3 - Ressource fermée par `Scope`

**Idée du jour :** une ressource doit être libérée en succès, en échec, et en interruption. ZIO met la durée de vie de la ressource dans le programme.

### Scala de base : `try/finally` fonctionne, mais se compose mal

```scala
import java.io.{BufferedReader, StringReader}

val reader = new BufferedReader(new StringReader("tx-1,AWB,CIH,100"))
try
  val line = reader.readLine()
  println(line)
finally
  reader.close()
```

**Limite :** `try/finally` protège ce bloc précis. Dès que la lecture devient asynchrone, concurrente, ou composée avec d'autres ressources, le raisonnement devient plus fragile.

### ZIO : la ressource vit dans un `Scope`

**Fichier à créer :** `src/main/scala/distributed/zio/ResourceObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*
import java.io.{BufferedReader, StringReader}

object ResourceObservation extends ZIOAppDefault:

  // CSV en mémoire pour éviter les problèmes de chemin de fichier.
  // Le but est d'observer acquire/release, pas de parser un gros batch.
  private val csv =
    """tx-1,AWB,CIH,100
      |tx-2,CIH,AWB,40
      |tx-3,AWB,CIH,10
      |""".stripMargin

  // Rôle : acquérir une ressource et déclarer son finaliseur.
  // Pourquoi ZIO[Scope, Nothing, BufferedReader] :
  // le type indique que le reader ne peut vivre qu'à l'intérieur d'un Scope.
  private def openReader: ZIO[Scope, Nothing, BufferedReader] =
    ZIO.acquireRelease(
      printLine("acquire reader").orDie.as(new BufferedReader(new StringReader(csv)))
    ) { reader =>
      ZIO.attempt(reader.close()).orDie *> printLine("release reader").orDie
    }

  // Rôle : lire les lignes une par une.
  // Pourquoi retourner IO[String, List[String]] :
  // on montre une erreur contrôlée sans lancer volontairement une exception.
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

  // Rôle : borner la durée de vie du reader.
  // Pourquoi ZIO.scoped : à la sortie du bloc, ZIO exécute release.
  def readAll(failAfterFirst: Boolean): IO[String, List[String]] =
    ZIO.scoped {
      for
        reader <- openReader
        lines  <- readLoop(reader, Nil, failAfterFirst)
      yield lines
    }

  // Changer failAfterFirst en true prouve que release s'exécute aussi en erreur.
  def run =
    readAll(failAfterFirst = false).foldZIO(
      error => printLine(s"erreur observée : $error").orDie,
      lines => printLine(s"${lines.size} ligne(s) lue(s)").orDie
    )
```

**À observer :**

- `acquire reader` apparaît avant la lecture.
- `release reader` apparaît en succès et en échec.
- Le `BufferedReader` ne sort pas du `Scope`.

---

## Kit 14.4 - Concurrence bornée et timeout

**Idée du jour :** ZIO permet de lancer des effets en parallèle tout en gardant une borne. Le stagiaire observe le gain de temps sans gérer lui-même un pool de threads.

### Scala de base : `Future` démarre vite, mais le contrôle est implicite

```scala
import scala.concurrent.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.*

val a = Future { Thread.sleep(500); "A ok" }
val b = Future { Thread.sleep(500); "B ok" }

// Les Futures démarrent dès leur création.
val both = Future.sequence(List(a, b))
```

**Limite :** l'exécution démarre immédiatement, le pool vient d'un implicite global, et la borne de parallélisme n'est pas visible dans le type du programme.

### ZIO : le parallélisme est un choix local et mesurable

**Fichier à créer :** `src/main/scala/distributed/zio/ParallelObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*

object ParallelObservation extends ZIOAppDefault:

  final case class Batch(name: String, delay: Duration)

  // Quatre batchs identiques rendent la comparaison facile :
  // séquentiel ≈ 4 x 500 ms, parallèle x2 ≈ 2 vagues.
  private val batches = List(
    Batch("A", 500.millis),
    Batch("B", 500.millis),
    Batch("C", 500.millis),
    Batch("D", 500.millis)
  )

  // Rôle : simuler un appel I/O ou réseau.
  // Pourquoi ZIO.sleep : l'attente est non bloquante pour le runtime ZIO.
  private def process(batch: Batch): UIO[String] =
    ZIO.sleep(batch.delay) *> ZIO.succeed(s"${batch.name} ok")

  // Rôle : mesurer sans sortir de ZIO.
  private def measure[A](label: String)(effect: UIO[A]): UIO[A] =
    for
      start <- Clock.nanoTime
      value <- effect
      end   <- Clock.nanoTime
      _     <- printLine(s"$label : ${(end - start) / 1000000} ms").orDie
    yield value

  def run =
    for
      // foreach garde l'ordre et exécute les effets l'un après l'autre.
      _ <- measure("séquentiel")(ZIO.foreach(batches)(process))

      // foreachPar lance plusieurs effets.
      // withParallelism(2) rend la borne explicite : au plus deux batchs actifs.
      _ <- measure("parallèle x2")(ZIO.foreachPar(batches)(process).withParallelism(2))

      // timeout transforme un effet trop lent en Option.
      // None signifie : le résultat n'est pas arrivé à temps.
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

**Idée du jour :** une erreur temporaire peut être retentée; une erreur définitive doit échouer vite. Un circuit ouvert évite d'aggraver une panne.

### Scala de base : une boucle de retry mélange tout

```scala
def retryBase[A](maxRetries: Int)(call: => A): A =
  var remaining = maxRetries
  var lastError: Throwable = null

  while remaining >= 0 do
    try return call
    catch
      case e: Throwable =>
        lastError = e
        remaining -= 1

  throw lastError
```

**Limite :** cette boucle ne distingue pas les erreurs temporaires des erreurs définitives. Elle mélange la politique de retry, l'appel distant, l'attente, et la gestion d'erreur.

### ZIO : la politique de retry est une valeur

**Fichier à créer :** `src/main/scala/distributed/zio/RetryObservation.scala`

```scala
package distributed.zio

import zio.*
import zio.Console.*

object RetryObservation extends ZIOAppDefault:

  // Deux erreurs observables :
  // - Temporary : retry autorisé;
  // - InvalidCurrency : erreur définitive, pas de retry.
  enum RateError:
    case Temporary(message: String)
    case InvalidCurrency(value: String)

  // Rôle : décrire quand retenter.
  // Pourquoi Schedule : la stratégie devient une valeur testable et réutilisable.
  private val temporaryOnly: Schedule[Any, RateError, Any] =
    (Schedule.exponential(100.millis) && Schedule.recurs(3))
      .whileInput {
        case RateError.Temporary(_)       => true
        case RateError.InvalidCurrency(_) => false
      }

  // Rôle : simuler un service instable de manière déterministe.
  // Le compteur permet d'observer exactement quatre appels pour EUR.
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

  // Rôle : montrer le principe du circuit ouvert.
  // Si circuitOpen vaut true, on échoue avant d'appeler le service distant.
  private def guarded(counter: Ref[Int], circuitOpen: Ref[Boolean]): IO[RateError, BigDecimal] =
    circuitOpen.get.flatMap {
      case true =>
        printLine("circuit ouvert : aucun appel distant").orDie *>
          ZIO.fail(RateError.Temporary("circuit ouvert"))
      case false =>
        flaky(counter, "EUR").tapError {
          case RateError.Temporary(_)       => circuitOpen.set(true)
          case RateError.InvalidCurrency(_) => ZIO.unit
        }
    }

  def run =
    for
      counter <- Ref.make(0)

      // EUR échoue trois fois, puis réussit.
      ok <- flaky(counter, "EUR").retry(temporaryOnly).either
      _  <- printLine(s"retry EUR : $ok").orDie

      // EURO est invalide : la politique refuse de retenter.
      invalid <- flaky(counter, "EURO").retry(temporaryOnly).either
      _       <- printLine(s"retry EURO : $invalid").orDie

      // Circuit ouvert : aucun nouvel appel distant.
      open    <- Ref.make(true)
      blocked <- guarded(counter, open).either
      _       <- printLine(s"circuit : $blocked").orDie
    yield ()
```

**À observer :**

- `EUR` réussit au quatrième appel.
- `EURO` échoue sans retry, car l'erreur n'est pas temporaire.
- Le circuit ouvert bloque avant l'appel distant.
