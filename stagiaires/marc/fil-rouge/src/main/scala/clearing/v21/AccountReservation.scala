package clearing.v21

case class RailAccount(id: String, balance: BigDecimal)

enum ReservationError:
  case AccountNotFound(id: String)
  case NonPositiveAmount(amount: BigDecimal)
  case InsufficientBalance(
    accountId: String,
    available: BigDecimal,
    requested: BigDecimal
  )

object AccountReservation:
  def findAccount(
    id: String,
    accounts: Map[String, RailAccount]
  ): Either[ReservationError, RailAccount] =
    accounts.get(id).toRight(ReservationError.AccountNotFound(id))

  def checkBalance(
    amount: BigDecimal
  )(
    account: RailAccount
  ): Either[ReservationError, RailAccount] =
    if amount <= 0 then Left(ReservationError.NonPositiveAmount(amount))
    else if account.balance < amount then
      Left(
        ReservationError.InsufficientBalance(
          account.id,
          account.balance,
          amount
        )
      )
    else Right(account)

  def reserveFunds(
    amount: BigDecimal
  )(
    account: RailAccount
  ): Either[ReservationError, RailAccount] =
    Right(account.copy(balance = account.balance - amount))

  def reserve(
    id: String,
    amount: BigDecimal,
    accounts: Map[String, RailAccount]
  ): Either[ReservationError, RailAccount] =
    for
      account <- findAccount(id, accounts)
      funded <- checkBalance(amount)(account)
      reserved <- reserveFunds(amount)(funded)
    yield reserved
