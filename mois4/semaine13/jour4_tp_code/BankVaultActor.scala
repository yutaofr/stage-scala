package distributed.actors

import org.apache.pekko.actor.typed.{ActorRef, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors
import scala.util.Random

/**
 * Corrigé exécutable de l'acteur BankVault (Kit 13.4 + Kit 13.7).
 *
 * - Credit / Debit : logique métier (Exercice 1).
 * - Thread.sleep aléatoire : asynchronisme (Exercice 3).
 *
 * État (balance) porté par récursion : pas de `var`, pas de `synchronized`.
 * Un seul message traité à la fois → aucune race condition possible.
 */
object BankVaultActor:
  sealed trait Command
  case class Credit(amount: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class Debit(amount: BigDecimal, replyTo: ActorRef[Response]) extends Command
  case class GetBalance(replyTo: ActorRef[Response]) extends Command

  // La réponse porte le `bankId` : un même destinataire (ex. le ClearingManager)
  // reçoit les réponses de PLUSIEURS coffres dans une seule mailbox ; il doit
  // pouvoir savoir QUI répond. (Voir aussi la règle "1 messageAdapter par classe".)
  sealed trait Response
  case class BalanceResponse(bankId: String, balance: BigDecimal) extends Response
  case class TransactionResult(bankId: String, success: Boolean, newBalance: BigDecimal) extends Response

  def apply(bankId: String, initialBalance: BigDecimal = BigDecimal(0)): Behavior[Command] =
    active(bankId, initialBalance)

  private def active(bankId: String, balance: BigDecimal): Behavior[Command] =
    Behaviors.receive { (context, message) =>
      message match
        case Credit(amount, replyTo) =>
          Thread.sleep(Random.between(50, 300)) // Ex 3 : simule un traitement lent
          val newBalance = balance + amount
          context.log.info(s"[$bankId] Crédit de $amount → Nouveau solde : $newBalance")
          replyTo ! TransactionResult(bankId, true, newBalance)
          active(bankId, newBalance)

        case Debit(amount, replyTo) =>
          Thread.sleep(Random.between(50, 300)) // Ex 3
          if balance >= amount then
            val newBalance = balance - amount
            context.log.info(s"[$bankId] Débit de $amount → Nouveau solde : $newBalance")
            replyTo ! TransactionResult(bankId, true, newBalance)
            active(bankId, newBalance)
          else
            context.log.warn(s"[$bankId] Débit de $amount REFUSÉ (solde : $balance)")
            replyTo ! TransactionResult(bankId, false, balance)
            Behaviors.same

        case GetBalance(replyTo) =>
          replyTo ! BalanceResponse(bankId, balance)
          Behaviors.same
    }
