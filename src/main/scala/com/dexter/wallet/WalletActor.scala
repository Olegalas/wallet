package com.dexter.wallet

import akka.actor.{Actor, ActorLogging, Props}
import com.dexter.wallet.model.DomainModel.{DecreaseBalance, GetBalance, IncreaseBalance, InitialTransaction, Wallet, Wallets}
import com.dexter.wallet.model.DomainResponse.Failure
import com.dexter.wallet.model.Identity.{AdminUser, RegularUser}

import scala.collection.mutable
import scala.language.reflectiveCalls

class WalletActor() extends Actor with ActorLogging {

  private type UpdateWalletCommand = {
    def account: String
    def money: Int
  }

  // Imagine it is persistent actor with state.
  private val state = mutable.HashMap(
    "Jason" -> Wallet("Jason", 1000, List(InitialTransaction(1000))),
    "James" -> Wallet("James", 5000, List(InitialTransaction(5000))),
    "Kirk" -> Wallet("James", 0, List(InitialTransaction(0))),
    "Lars" -> Wallet("James", 0, List(InitialTransaction(0)))
  )

  private def updateState(walletToUpdate: Wallet): Unit = {
    state.update(walletToUpdate.account, walletToUpdate)
  }

  private def processTransaction(wallet: => Wallet): Unit = {
    val updatedWallet = wallet
    updateState(updatedWallet)
    sender() ! updatedWallet
  }

  private def withWallet(account: String)(process: Wallet => Unit): Unit = {
    state.get(account) match {
      case Some(wallet) => process(wallet)
      case None => sender() ! Failure(s"Account: $account was not found")
    }
  }

  private def processCommand(cmd: UpdateWalletCommand): Unit = {
    withWallet(cmd.account) { wallet =>
      processTransaction {
        cmd match {
          case cmd: IncreaseBalance =>
            wallet.increase(cmd.money + feeCalculator(wallet.transactions.size))
          case cmd: DecreaseBalance =>
            wallet.decrease(cmd.money - feeCalculator(wallet.transactions.size))
        }
      }
    }
  }

  def receive: Receive = {
    case cmd: UpdateWalletCommand => processCommand(cmd)
    case query: GetBalance =>
      query.identity match {
        case AdminUser(_) =>
          sender() ! Wallets(state.values.toList)
        case RegularUser(_) =>
          withWallet(query.identity.accountName) { wallet =>
            sender() ! wallet
          }
      }
    case x => log.warning("Received unknown message: {}", x)
  }

  // Let it be in Int.. just for testing
  private def feeCalculator(transactions: Int): Int = {
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
