package com.dexter.wallet

import akka.actor.{Actor, ActorLogging, Props}
import com.dexter.wallet.model.DomainModel.{DecreaseBalance, GetBalance, IncreaseBalance, InitialTransaction, Wallet, Wallets}
import com.dexter.wallet.model.DomainResponse.Failure
import com.dexter.wallet.model.Identity.{AdminUser, RegularUser}

class WalletActor() extends Actor with ActorLogging {

  // Imagine it is persistent actor with state.
  var state = Map(
    "Jason" -> Wallet("Jason", 1000, List(InitialTransaction(1000))),
    "James" -> Wallet("James", 5000, List(InitialTransaction(5000))),
    "Kirk" -> Wallet("James", 0, List(InitialTransaction(0))),
    "Lars" -> Wallet("James", 0, List(InitialTransaction(0)))
  )

  // FIXME: function with side effect
  def updateState(walletToUpdate: Wallet): Unit = {
    state = state.updated(walletToUpdate.account, walletToUpdate)
  }

  override def preStart(): Unit = {
    log.info("WalletActor actor was launched")
  }

  def processTransaction(wallet: => Wallet): Unit = {
    val updatedWallet = wallet
    updateState(updatedWallet)
    sender() ! updatedWallet
  }

  def receive: Receive = {
    case cmd: IncreaseBalance =>
      state.get(cmd.account) match {
        case Some(wallet) =>
          processTransaction {
            wallet.increase(cmd.money - feeCalculator(wallet.transactions.size))
          }
        case None => sender() ! Failure(s"Account: ${cmd.account} was not found")
      }
    case cmd: DecreaseBalance =>
      state.get(cmd.account) match {
        case Some(wallet) =>
          processTransaction {
            wallet.decrease(cmd.money + feeCalculator(wallet.transactions.size))
          }
        case None => sender() ! Failure(s"Account: ${cmd.account} was not found")
      }
    case cmd: GetBalance =>
      cmd.identity match {
        case AdminUser(_) =>
          sender() ! Wallets(state.values.toList)
        case RegularUser(_) =>
          state.get(cmd.identity.accountName) match {
            case Some(wallet) => sender() ! wallet
            case None => sender() ! Failure(s"Account: ${cmd.identity.accountName} was not found")
          }
      }

    case x => log.warning("Received unknown message: {}", x)
  }

  // Let it be in Int.. just for testing
  def feeCalculator(transactions: Int): Int = {
    transactions match {
      case count if count < 100 => 3
      case count if count > 100 || count < 1000 => 2
      case count if count > 3000 => 1
    }
  }

}

object WalletActor {
  def props: Props = Props(new WalletActor())
}
