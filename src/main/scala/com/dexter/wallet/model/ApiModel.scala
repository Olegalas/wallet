package com.dexter.wallet.model

trait ApiModel
object ApiModel {

  case class Transaction(money: Int, account: Option[String]) // provided only in case of admin

}
