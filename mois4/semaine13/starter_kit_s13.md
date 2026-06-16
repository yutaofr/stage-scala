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

**Exercice du stagiaire :** 
1. Constater que le solde réel ≠ solde attendu.
2. Écrire une version `safeDebit` avec `synchronized` et vérifier que le problème disparaît.
3. Écrire une version immuable avec `case class Account(balance: Double)` + `AtomicReference[Account]` :
   ```scala
   import java.util.concurrent.atomic.AtomicReference
   val account = AtomicReference(Account(10000.0))
   def atomicDebit(amount: Double): Unit =
     account.updateAndGet(a => Account(a.balance - amount))
   ```
4. Comparer les trois approches (unsafe / synchronized / atomic+immutable) et expliquer laquelle est préférable et pourquoi.

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
 *
 * NOTE : Les types `clearing.Transaction` et `clearing.ClearingError`
 * proviennent du package `clearing` défini dans les TPs du Mois 3.
 * Assure-toi que ce package est accessible dans ton classpath.
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

  // 💡 Pattern "Récursion d'État" : l'état (balance) est porté par le paramètre
  // de la fonction récursive. Chaque nouveau message produit un nouvel appel
  // avec l'état mis à jour. Pas de `var` ! C'est la FP appliquée aux acteurs.
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

**Exercice du stagiaire :** Implémenter `Credit` et `Debit`. Lancer un `ActorSystem` avec 5 acteurs (un par banque) et leur envoyer des messages concurrents. Vérifier les soldes finaux.

---

## Kit 13.5 — Supervision Template (Pekko)

**Fichier fourni :** `src/main/scala/distributed/actors/SupervisedBanks.scala`

```scala
package distributed.actors

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior, SupervisorStrategy}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/**
 * STARTER KIT : La supervision est pré-câblée.
 * Le stagiaire définit les cas d'erreur et observe le redémarrage.
 */
object SupervisedBanks:

  // === ZONE TUTEUR : Infrastructure de supervision ===

  /** Crée un acteur BankVault supervisé : il redémarre automatiquement en cas de crash */
  def supervisedVault(bankId: String, initialBalance: BigDecimal): Behavior[BankVaultActor.Command] =
    Behaviors.supervise(
      BankVaultActor(bankId, initialBalance)
    ).onFailure[Exception](SupervisorStrategy.restart)

  /** Acteur racine qui crée et gère les acteurs bancaires */
  sealed trait ManagerCommand
  case class CreateBank(bankId: String, initialBalance: BigDecimal) extends ManagerCommand
  case class SendToBank(bankId: String, command: BankVaultActor.Command) extends ManagerCommand
  case object ListBanks extends ManagerCommand

  def bankManager(): Behavior[ManagerCommand] =
    manage(Map.empty)

  private def manage(banks: Map[String, ActorRef[BankVaultActor.Command]]): Behavior[ManagerCommand] =
    Behaviors.receive { (context, message) =>
      message match
        case CreateBank(bankId, initialBalance) =>
          context.log.info(s"Création de la banque $bankId")
          val ref = context.spawn(supervisedVault(bankId, initialBalance), bankId)
          manage(banks + (bankId -> ref))

        case SendToBank(bankId, command) =>
          // === ZONE STAGIAIRE ===
          // TODO : Trouver l'acteur dans la map `banks` et lui transmettre le message
          // Si la banque n'existe pas, logger un warning
          ???

        case ListBanks =>
          banks.keys.foreach(id => context.log.info(s"Banque active : $id"))
          Behaviors.same
    }

  // === ZONE TUTEUR : Runner pré-câblé ===

  @main def supervisionDemo(): Unit =
    val system = ActorSystem(bankManager(), "clearing-system")
    // Le stagiaire utilisera ce système pour :
    // 1. Créer des banques
    // 2. Envoyer des messages (y compris des montants qui provoquent des erreurs)
    // 3. Observer que les acteurs redémarrent et continuent de fonctionner
    Thread.sleep(5000)
    system.terminate()
```

**Exercice du stagiaire :**
1. Implémenter le `SendToBank` (lookup dans la map + envoi du message).
2. Modifier `BankVaultActor` pour lever une `RuntimeException` si le montant de débit est exactement `999`.
3. Envoyer un message `Debit(999)` et observer que l'acteur redémarre (le solde est réinitialisé).
4. Comparer avec un crash dans une boucle `for` classique.

> [!CAUTION]
> **Perte d'état au Restart** : Après un restart, le solde de l'acteur est réinitialisé à `initialBalance`. En production, on résout ce problème avec l'**Event Sourcing** (persister chaque message, rejouer au redémarrage). Ce sera un sujet futur.
