package com.dexter.wallet

import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.dexter.wallet.model.DomainModel.{DecreaseBalance, GetBalance, IncreaseBalance}
import com.dexter.wallet.model.DomainResponse.InternalServerError
import com.dexter.wallet.model.{ApiModel, DomainModel, DomainResponse, Identity}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContextExecutor, Future}

class WalletService(walletActor: ActorRef)(implicit system: ActorSystem) {
  private val log = system.log
  private implicit val executionContext: ExecutionContextExecutor = system.getDispatcher
  private implicit val timeout: Timeout = Timeout(1, TimeUnit.SECONDS)

  def increase(transaction: ApiModel.Transaction, identity: Identity): Future[DomainResponse] = {
    val effectiveUserName = transaction.account.getOrElse(identity.accountName)
    (walletActor ? IncreaseBalance(effectiveUserName, transaction.money)).map {
      case err: DomainResponse.Failure => err
      case resp: DomainModel.Wallet =>
        DomainResponse.Wallet(resp)
      case err => unexpectedResp(err)
    }
  }

  def decrease(transaction: ApiModel.Transaction, identity: Identity): Future[DomainResponse] = {
    val effectiveUserName = transaction.account.getOrElse(identity.accountName)
    (walletActor ? DecreaseBalance(effectiveUserName, transaction.money)).map {
      case err: DomainResponse.Failure => err
      case resp: DomainModel.Wallet =>
        DomainResponse.Wallet(resp)
      case err => unexpectedResp(err)
    }
  }

  def balance(identity: Identity): Future[DomainResponse] = {
    (walletActor ? GetBalance(identity)).map {
      case resp: DomainModel.Wallets =>
        DomainResponse.Wallets(resp.wallets.map(DomainResponse.Wallet.apply))
      case resp: DomainModel.Wallet =>
        DomainResponse.Wallet(resp)
      case err: DomainResponse.Failure => err
      case err => unexpectedResp(err)
    }
  }

  private def unexpectedResp(err: Any): DomainResponse = {
    log.error("Unexpected response: {}", err)
    InternalServerError
  }
}
