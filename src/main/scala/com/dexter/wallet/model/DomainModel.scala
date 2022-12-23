package com.dexter.wallet.model

object DomainModel {

  // Commands
  case class IncreaseBalance(account: String, money: Int)

  case class DecreaseBalance(account: String, money: Int)

  case class GetBalance(identity: Identity)

  // Domain model

  sealed trait Transaction

  case class InitialTransaction(initialAmount: Int) extends Transaction

  case class IncreaseTransaction(increase: Int) extends Transaction

  case class DecreaseTransaction(decrease: Int) extends Transaction

  case class Wallet(account: String, balance: Int, transactions: List[Transaction]) {
    def decrease(money: Int): Wallet =
      this.copy(balance = balance - money, transactions = IncreaseTransaction(money) :: transactions)

    def increase(money: Int): Wallet =
      this.copy(balance = balance + money, transactions = DecreaseTransaction(money) :: transactions)
  }

  case class Wallets(wallets: List[Wallet])

}
