package distributed.actors

import org.apache.pekko.actor.typed.{ActorRef, ActorSystem, Behavior}
import org.apache.pekko.actor.typed.scaladsl.Behaviors

/**
 * Corrigé exécutable de l'orchestrateur (Kit 13.6, Exercice 2).
 *
 * Le manager reçoit les positions nettes (bankId -> net) et envoie un
 * Credit (net > 0) ou un Debit (net < 0) au BON acteur BankVault, retrouvé
 * dans son annuaire `vaults: Map[String, ActorRef[...]]`.
 */
object ClearingManager:

  sealed trait Command
  /** Applique un résultat de netting : banque -> position nette (+ créditeur / − débiteur). */
  case class ApplyNetting(positions: Map[String, BigDecimal]) extends Command
  /** Message interne : on "replie" les réponses des BankVault dans NOTRE protocole. */
  private case class WrappedResult(response: BankVaultActor.Response) extends Command

  /** @param vaults l'annuaire : bankId -> référence de SON acteur (le "carnet d'adresses"). */
  def apply(vaults: Map[String, ActorRef[BankVaultActor.Command]]): Behavior[Command] =
    Behaviors.setup { context =>
      // UN SEUL adaptateur : Pekko n'enregistre qu'un adaptateur PAR CLASSE de message.
      // C'est pourquoi la réponse porte elle-même son `bankId` (pas l'adaptateur).
      val responseAdapter: ActorRef[BankVaultActor.Response] =
        context.messageAdapter(resp => WrappedResult(resp))

      Behaviors.receiveMessage {
        case ApplyNetting(positions) =>
          positions.foreach { (bankId, net) =>
            vaults.get(bankId) match
              case Some(vaultRef) =>
                // Décision crédit / débit selon le signe de la position nette.
                if net > 0 then
                  vaultRef ! BankVaultActor.Credit(net, responseAdapter)
                else if net < 0 then
                  vaultRef ! BankVaultActor.Debit(-net, responseAdapter)
                else
                  context.log.info(s"[$bankId] position nette nulle, rien à faire")
              case None =>
                context.log.warn(s"Aucun coffre connu pour la banque '$bankId' — message ignoré")
          }
          Behaviors.same

        case WrappedResult(BankVaultActor.TransactionResult(bankId, success, newBalance)) =>
          val verdict = if success then "OK" else "REFUSÉE (solde insuffisant)"
          context.log.info(s"[$bankId] Transaction $verdict → solde = $newBalance")
          Behaviors.same

        case WrappedResult(BankVaultActor.BalanceResponse(bankId, balance)) =>
          context.log.info(s"[$bankId] Solde = $balance")
          Behaviors.same
      }
    }

  @main def clearingDemo(): Unit =
    val root: Behavior[Nothing] = Behaviors.setup[Nothing] { context =>
      // 1) Un acteur (= un coffre) par banque. Chaque spawn = un acteur DISTINCT.
      val banques = List("BMCE" -> BigDecimal(1000), "CIH" -> BigDecimal(1000), "BP" -> BigDecimal(1000))
      val vaults: Map[String, ActorRef[BankVaultActor.Command]] =
        banques.map { (id, solde) => id -> context.spawn(BankVaultActor(id, solde), id) }.toMap

      // 2) Le manager reçoit l'annuaire des coffres.
      val manager = context.spawn(ClearingManager(vaults), "clearing-manager")

      // 3) Un résultat de netting (somme nette = 0, comme un vrai clearing).
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
