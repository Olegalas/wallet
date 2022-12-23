package com.dexter.wallet.model

sealed trait DomainResponse

object DomainResponse {

  case class Failure(message: String) extends DomainResponse

  object InternalServerError extends DomainResponse

  sealed trait Transaction

  case class InitialTransaction(initialAmount: Int) extends Transaction

  case class IncreaseTransaction(increase: Int) extends Transaction

  case class DecreaseTransaction(decrease: Int) extends Transaction

  case class Wallet(account: String, balance: Int, transactions: List[Transaction]) extends DomainResponse

  object Wallet {
    def apply(wallet: DomainModel.Wallet): Wallet = {
      val transactions = wallet.transactions.map {
        case DomainModel.InitialTransaction(amount) => InitialTransaction(amount)
        case DomainModel.IncreaseTransaction(amount) => IncreaseTransaction(amount)
        case DomainModel.DecreaseTransaction(amount) => DecreaseTransaction(amount)
      }
      new Wallet(wallet.account, wallet.balance, transactions)
    }
  }

  case class Wallets(wallets: List[Wallet]) extends DomainResponse

}
