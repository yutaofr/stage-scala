package clearing.v10

import clearing.model.*

object TransactionRouterV10:
  def route(transaction: Transaction): String =
    transaction.status match
      case TransactionStatus.Pending    => "EN_ATTENTE"
      case TransactionStatus.Validated  => "COMPENSATION"
      case TransactionStatus.Rejected   => "REJET"
      case TransactionStatus.Suspicious => "CONTROLE_MANUEL"
