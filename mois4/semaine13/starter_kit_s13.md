# Starter Kit Semaine 13 : Concurrence Native

Ce starter kit fournit les fondations pour que le stagiaire se concentre sur l'implémentation de la logique métier, pas sur le wiring de la concurrence.

---

## Kit 13.1 — Race Condition Simulator

**Fichier fourni :** `src/main/scala/distributed/concurrency/RaceDemo.scala`

```scala
package distributed.concurrency

/**
 * STARTER KIT : Ce fichier est pré-câblé par le tuteur.
 * 
 * L'objectif du stagiaire : comprendre POURQUOI le résultat
 * est incohérent, puis corriger en utilisant l'immuabilité.
 */
object RaceDemo:
  // ⚠️ CODE VOLONTAIREMENT BUGUÉ — Pour illustrer le problème
  var sharedBalance: Double = 10000.0

  def unsafeDebit(amount: Double): Unit =
    val current = sharedBalance
    Thread.sleep(1) // Simule un délai réseau
    sharedBalance = current - amount

  @main def showRaceCondition(): Unit =
    println(s"Solde initial : $sharedBalance")

    val threads = (1 to 100).map { _ =>
      new Thread(() => unsafeDebit(10.0))
    }
    threads.foreach(_.start())
    threads.foreach(_.join())

    println(s"Solde attendu : ${10000.0 - 100 * 10.0}")
    println(s"Solde réel    : $sharedBalance")
    println(s"Différence    : ${sharedBalance - (10000.0 - 100 * 10.0)}")
    // 👆 Le stagiaire constatera que le solde réel ≠ solde attendu
```

**Exercice du stagiaire :** Écrire une version `safeDebit` en utilisant un `val` et du code immuable. Expliquer pourquoi le problème disparaît.

---

## Kit 13.2 — Future Pipeline Template

**Fichier fourni :** `src/main/scala/distributed/concurrency/FuturePipeline.scala`

```scala
package distributed.concurrency

import scala.concurrent.{Future, ExecutionContext, Await}
import scala.concurrent.duration.*

/**
 * STARTER KIT : Le pipeline parallèle est pré-câblé.
 * Le stagiaire implémente uniquement les fonctions métier.
 */
object FuturePipeline:
  given ec: ExecutionContext = ExecutionContext.global

  // === ZONE STAGIAIRE : Implémenter ces fonctions ===

  /** Valide une transaction (simuler un délai réseau de 200ms) */
  def validateAsync(tx: clearing.Transaction): Future[Either[String, clearing.Transaction]] =
    Future {
      Thread.sleep(200) // Simule un appel réseau
      ???  // TODO : implémenter la validation
    }

  /** Calcule la position nette d'un batch */
  def calculateNetting(txs: List[clearing.Transaction]): Future[Map[String, BigDecimal]] =
    Future {
      ???  // TODO : implémenter le netting (réutiliser le code du Mois 3)
    }

  // === ZONE TUTEUR : Infrastructure pré-câblée ===

  def runParallelValidation(transactions: List[clearing.Transaction]): Future[List[Either[String, clearing.Transaction]]] =
    Future.sequence(transactions.map(validateAsync))

  def runWithTimeout[T](future: Future[T], timeout: Duration = 5.seconds): T =
    Await.result(future, timeout)

  @main def demo(): Unit =
    val transactions = ??? // Générer 10 transactions de test
    val results = runWithTimeout(runParallelValidation(transactions))
    val (errors, valid) = results.partitionMap(identity)
    println(s"Validées : ${valid.size} | Rejetées : ${errors.size}")

    val netting = runWithTimeout(calculateNetting(valid))
    netting.foreach { (bank, net) =>
      println(f"$bank%-6s : $net%12.2f DH")
    }
```

---

## Kit 13.3 — ExecutionContext Custom

**Fichier fourni :** `src/main/scala/distributed/concurrency/CustomPools.scala`

```scala
package distributed.concurrency

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

/**
 * STARTER KIT : Les pools sont pré-configurés.
 * Le stagiaire les utilise pour séparer CPU et I/O.
 */
object CustomPools:
  // Pool dédié au calcul (nombre de cores)
  val cpuPool: ExecutionContext =
    ExecutionContext.fromExecutor(
      Executors.newFixedThreadPool(Runtime.getRuntime.availableProcessors())
    )

  // Pool dédié aux I/O (élastique)
  val ioPool: ExecutionContext =
    ExecutionContext.fromExecutor(
      Executors.newCachedThreadPool()
    )

  def shutdown(): Unit =
    cpuPool match
      case e: java.util.concurrent.ExecutorService => e.shutdown()
      case _ =>
    ioPool match
      case e: java.util.concurrent.ExecutorService => e.shutdown()
      case _ =>
```

**Exercice du stagiaire :** Lancer le netting sur `cpuPool` et la lecture de fichiers sur `ioPool`. Benchmark : comparer avec `ExecutionContext.global`.

---

## Kit 13.4 — Acteur BankVault (Pekko)

**Fichier fourni :** `src/main/scala/distributed/actors/BankVaultActor.scala`

```scala
package distributed.actors

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/**
 * STARTER KIT : Le squelette de l'acteur est fourni.
 * Le stagiaire implémente la logique dans "handle".
 */
object BankVaultActor:
  // Messages que l'acteur comprend
  sealed trait Command
  case class Credit(amount: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class Debit(amount: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class GetBalance(replyTo: ActorRef[Response]) extends Command

  // Réponses
  sealed trait Response
  case class BalanceResponse(balance: BigDecimal) extends Response
  case class TransactionResult(success: Boolean, newBalance: BigDecimal) extends Response

  def apply(bankId: String, initialBalance: BigDecimal = BigDecimal(0)): Behavior[Command] =
    active(bankId, initialBalance)

  private def active(bankId: String, balance: BigDecimal): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case Credit(amount, replyTo) =>
          // === ZONE STAGIAIRE ===
          val newBalance = ???  // TODO : calculer le nouveau solde
          context.log.info(s"[$bankId] Crédit de $amount → Nouveau solde : $newBalance")
          replyTo ! TransactionResult(true, newBalance)
          active(bankId, newBalance)

        case Debit(amount, replyTo) =>
          // === ZONE STAGIAIRE ===
          // TODO : Vérifier que le solde est suffisant
          // Si oui : débiter et retourner TransactionResult(true, ...)
          // Si non : retourner TransactionResult(false, balance) sans modifier
          ???

        case GetBalance(replyTo) =>
          replyTo ! BalanceResponse(balance)
          Behaviors.same
    }
```

**Exercice du stagiaire :** Implémenter `Credit` et `Debit`. Lancer 5 acteurs (un par banque) et leur envoyer des messages concurrents.
