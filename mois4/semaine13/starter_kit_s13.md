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
 * NOTE : Le domaine vient du cœur v2.3. Il est aussi fourni dans
 * `fil-rouge/src/main/scala/clearing` pour éviter toute redéfinition locale.
 */
object FuturePipeline:
  given ec: ExecutionContext = ExecutionContext.global

  import clearing.core.ClearingError
  import clearing.model.*

  // === ZONE STAGIAIRE : Implémenter ces fonctions ===

  /** Valide une transaction (simuler un délai réseau de 200ms) */
  def validateAsync(tx: Transaction): Future[Either[ClearingError, Transaction]] =
    Future {
      Thread.sleep(200) // Simule un appel réseau
      ???  // TODO : implémenter la validation
    }

  /** Calcule la position nette d'un batch */
  def calculateNetting(txs: List[Transaction]): Future[Map[BankCode, Money]] =
    Future {
      ???  // TODO : implémenter le netting (réutiliser le code du Mois 3)
    }

  // === ZONE TUTEUR : Infrastructure pré-câblée ===

  def runParallelValidation(transactions: List[Transaction]): Future[List[Either[ClearingError, Transaction]]] =
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
      println(f"${bank.value}%-6s : ${net.value}%12.2f DH")
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

  // Réponses — elles portent le `bankId` : un même destinataire (le ClearingManager)
  // reçoit les réponses de plusieurs coffres dans UNE seule mailbox et doit savoir QUI répond.
  sealed trait Response
  case class BalanceResponse(bankId: String, balance: BigDecimal) extends Response
  case class TransactionResult(bankId: String, success: Boolean, newBalance: BigDecimal) extends Response

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
          replyTo ! TransactionResult(bankId, true, newBalance)
          active(bankId, newBalance)

        case Debit(amount, replyTo) =>
          // === ZONE STAGIAIRE ===
          // TODO : Vérifier que le solde est suffisant
          // Si oui : débiter et retourner TransactionResult(bankId, true, ...)
          // Si non : retourner TransactionResult(bankId, false, balance) sans modifier
          ???

        case GetBalance(replyTo) =>
          replyTo ! BalanceResponse(bankId, balance)
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

---

## Kit 13.6 — ClearingManager / Orchestrateur (Pekko) — **Exercice 2**

**Fichier fourni :** `src/main/scala/distributed/actors/ClearingManager.scala`

> [!TIP]
> **Idée clé.** Le `ClearingManager` reçoit les **positions nettes** calculées par le netting (`Map[bankId -> position]`, cf. Kit 13.2) et, pour chaque banque, envoie un `Credit` (position positive) ou un `Debit` (position négative) au **bon** acteur `BankVault`.
>
> Comment trouve-t-il le « bon » acteur ? Il porte un **annuaire** `Map[String, ActorRef[BankVaultActor.Command]]` (banque → référence d'acteur). En Pekko Typed, il n'y a pas de recherche d'acteur par nom : **on dispatch en détenant l'`ActorRef`.**

```scala
package distributed.actors

import clearing.model.*
import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/**
 * STARTER KIT : Le protocole, l'annuaire d'acteurs et le routage des réponses
 * sont pré-câblés (ZONE TUTEUR). Le stagiaire écrit UNIQUEMENT la décision
 * "crédit ou débit ?" et le dispatch via `!` (ZONE STAGIAIRE).
 */
object ClearingManager:

  // === ZONE TUTEUR : Protocole de messages ===
  sealed trait Command
  /** Applique un résultat de netting : banque -> position nette (+ créditeur / − débiteur). */
  case class ApplyNetting(positions: Map[BankCode, Money]) extends Command
  /** Message INTERNE : on "replie" les réponses des BankVault dans NOTRE protocole. */
  private case class WrappedResult(response: BankVaultActor.Response) extends Command

  /**
   * @param vaults l'ANNUAIRE : à chaque bankId correspond la référence de SON acteur.
   *               C'est ce `Map` qui permet de "trouver" l'acteur destinataire.
   */
  def apply(vaults: Map[BankCode, ActorRef[BankVaultActor.Command]]): Behavior[Command] =
    Behaviors.setup { context =>
      // UN SEUL adaptateur : Pekko n'enregistre qu'UN adaptateur PAR CLASSE de message
      // (un 2e appel pour la même classe REMPLACE le 1er). C'est pourquoi la réponse
      // porte elle-même son `bankId` — et non l'adaptateur.
      val responseAdapter: ActorRef[BankVaultActor.Response] =
        context.messageAdapter(resp => WrappedResult(resp))

      Behaviors.receiveMessage {
        case ApplyNetting(positions) =>
          positions.foreach { (bankId, net) =>
            vaults.get(bankId) match
              case Some(vaultRef) =>
                // === ZONE STAGIAIRE ===
                // TODO : selon le SIGNE de `net`, envoyer le bon message au coffre :
                //   - net.value > 0  → vaultRef ! BankVaultActor.Credit(net.value, responseAdapter)
                //   - net.value < 0  → vaultRef ! BankVaultActor.Debit(-net.value, responseAdapter)
                //   - net == 0 → rien (logger éventuellement "position nulle")
                ???
              case None =>
                context.log.warn(s"Aucun coffre connu pour la banque '$bankId' — message ignoré")
          }
          Behaviors.same

        // ZONE TUTEUR : réception ASYNCHRONE des réponses (preuve du non-blocage)
        case WrappedResult(BankVaultActor.TransactionResult(bankId, success, newBalance)) =>
          val verdict = if success then "OK" else "REFUSÉE (solde insuffisant)"
          context.log.info(s"[$bankId] Transaction $verdict → solde = $newBalance")
          Behaviors.same

        case WrappedResult(BankVaultActor.BalanceResponse(bankId, balance)) =>
          context.log.info(s"[$bankId] Solde = $balance")
          Behaviors.same
      }
    }

  // === ZONE TUTEUR : Runner pré-câblé ===
  @main def clearingDemo(): Unit =
    val root: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      // 1) Créer un acteur (= un coffre) PAR banque. Chaque spawn = un acteur DISTINCT :
      //    mailbox propre, état propre (balance), chemin/identité unique (/user/<nom>).
      val banques = List("BMCE" -> BigDecimal(1000), "CIH" -> BigDecimal(1000), "BP" -> BigDecimal(1000))
      val vaults: Map[String, ActorRef[BankVaultActor.Command]] =
        banques.map { (id, solde) => id -> context.spawn(BankVaultActor(id, solde), id) }.toMap

      // 2) Créer le manager EN LUI DONNANT l'annuaire des coffres.
      val manager = context.spawn(ClearingManager(vaults), "clearing-manager")

      // 3) Lui envoyer un résultat de netting (somme nette = 0, comme un vrai clearing).
      manager ! ApplyNetting(Map(
        "BMCE" -> BigDecimal(250),   // créditeur net  → Credit(250)
        "CIH"  -> BigDecimal(-300),  // débiteur net   → Debit(300)
        "BP"   -> BigDecimal(50)     // créditeur net  → Credit(50)
      ))

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](root, "clearing-system")
    Thread.sleep(2000)
    system.terminate()
```

> [!CAUTION]
> **Piège Pekko vérifié à la compilation/exécution :** `context.messageAdapter` n'enregistre **qu'UN adaptateur par classe de message**. Créer un adaptateur par banque (toutes de type `Response`) ne fonctionne pas : le dernier écrase les précédents et **toutes** les réponses sont mal attribuées. La bonne solution est celle ci-dessus : **un seul adaptateur**, et c'est la **réponse qui porte son `bankId`**.

> [!TIP]
> **Plus simple, si le message adapter te paraît trop avancé pour le Jour 4 :** remplace tout le routage de réponses par `context.system.ignoreRef[BankVaultActor.Response]` comme `replyTo`. Le manager dispatch alors sans rien écouter en retour. Tu observeras quand même l'asynchronisme via les logs du `BankVault` (Exercice 3).

**Exercice du stagiaire :** Écrire la ZONE STAGIAIRE (décision crédit/débit selon le signe + `!`). Lancer `clearingDemo` et vérifier dans les logs que chaque banque reçoit la bonne opération.

---

## Kit 13.7 — Asynchronisme total — **Exercice 3**

Pas de nouveau fichier : on **modifie** `BankVaultActor.active` pour simuler un traitement lent, et on observe que le `ClearingManager` n'attend jamais.

```scala
// Dans BankVaultActor, AU DÉBUT du traitement de chaque message Credit/Debit :
import scala.util.Random

case Credit(amount, replyTo) =>
  Thread.sleep(Random.between(50, 300)) // simule un traitement lent (réseau, écriture disque…)
  val newBalance = balance + amount
  context.log.info(s"[$bankId] Crédit de $amount → Nouveau solde : $newBalance")
  replyTo ! TransactionResult(true, newBalance)
  active(bankId, newBalance)
```

**Ce qu'il faut observer :**
- Le `foreach` du `ClearingManager` envoie ses 3 messages avec `!` et **rend la main immédiatement** : les `!` ne font qu'**empiler** les messages dans les mailbox des coffres.
- Chaque `BankVault` traite SES messages un par un, chacun prenant un temps aléatoire. Les logs `TransactionResult` arrivent donc **plus tard et entrelacés** — preuve que rien n'a bloqué le manager.
- Plusieurs banques traitent **en parallèle** (acteurs distincts, threads distincts du dispatcher), mais **à l'intérieur d'un même coffre** l'ordre reste séquentiel (un seul message à la fois) → pas de race condition, sans `synchronized`.

> [!CAUTION]
> `Thread.sleep` dans un acteur **bloque le thread du dispatcher** : utile pour *illustrer* l'asynchronisme au Jour 4, mais c'est un anti-pattern en production (on préfère `Behaviors.withTimers` / messages planifiés, vus plus tard). À mentionner au stagiaire pour ne pas ancrer une mauvaise habitude.

---

## ✅ Corrigé de référence (à consulter APRÈS avoir essayé)

```scala
// --- Kit 13.4 : Credit / Debit ---
case Credit(amount, replyTo) =>
  val newBalance = balance + amount
  context.log.info(s"[$bankId] Crédit de $amount → Nouveau solde : $newBalance")
  replyTo ! TransactionResult(bankId, true, newBalance)
  active(bankId, newBalance)

case Debit(amount, replyTo) =>
  if balance >= amount then
    val newBalance = balance - amount
    context.log.info(s"[$bankId] Débit de $amount → Nouveau solde : $newBalance")
    replyTo ! TransactionResult(bankId, true, newBalance)
    active(bankId, newBalance)          // nouvel état
  else
    context.log.warn(s"[$bankId] Débit de $amount REFUSÉ (solde : $balance)")
    replyTo ! TransactionResult(bankId, false, balance)
    Behaviors.same                       // état INCHANGÉ

// --- Kit 13.6 : dispatch crédit/débit (ZONE STAGIAIRE) ---
if net > 0 then
  vaultRef ! BankVaultActor.Credit(net, responseAdapter)
else if net < 0 then
  vaultRef ! BankVaultActor.Debit(-net, responseAdapter)   // -net = montant positif à débiter
else
  context.log.info(s"[$bankId] position nette nulle, rien à faire")

// --- Kit 13.5 : SendToBank (même schéma de dispatch) ---
case SendToBank(bankId, command) =>
  banks.get(bankId) match
    case Some(ref) => ref ! command
    case None      => context.log.warn(s"Banque inconnue : $bankId")
  Behaviors.same
```
