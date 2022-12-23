package com.dexter.wallet

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import com.dexter.wallet.model.ApiModel.Transaction
import com.dexter.wallet.model.Identity.{AdminUser, RegularUser}
import com.dexter.wallet.model.{DomainResponse, Identity}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

class WalletRoutes(service: WalletService) {

  def myUserPassAuthenticator(credentials: Credentials): Option[Identity] =
    credentials match {
      case p@Credentials.Provided(id) if p.verify("company1Password") => Some(RegularUser(id))
      case p@Credentials.Provided(id) if p.identifier == "admin" && p.verify("adminPassword") => Some(AdminUser(id))
      case _ => None
    }

  def routes: Route = {
    path("balance") {
      val increase = post {
        authenticateBasic("balance routes", myUserPassAuthenticator) { identity =>
          entity(as[Transaction]) { transaction: Transaction =>
            onSuccess(service.increase(transaction, identity)) {
              defaultComplete
            }
          }
        }
      }
      val decrease = delete {
        authenticateBasic("balance routes", myUserPassAuthenticator) { identity =>
          entity(as[Transaction]) { transaction: Transaction =>
            onSuccess(service.decrease(transaction, identity)) {
              defaultComplete
            }
          }
        }
      }
      val balance = get {
        authenticateBasic("balance routes", myUserPassAuthenticator) { identity =>
          onSuccess(service.balance(identity)) {
            defaultComplete
          }
        }
      }
      increase ~ decrease ~ balance
    }
  }

  private def defaultComplete(resp: DomainResponse): Route = {
    resp match {
      case _: DomainResponse.Failure => complete(StatusCodes.BadRequest, resp)
      case DomainResponse.InternalServerError => complete(StatusCodes.InternalServerError, resp)
      case _: DomainResponse.Wallet => complete(StatusCodes.OK, resp)
      case _: DomainResponse.Wallets => complete(StatusCodes.OK, resp)
    }
  }

}
