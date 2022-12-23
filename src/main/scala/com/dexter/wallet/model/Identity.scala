package com.dexter.wallet.model

sealed trait Identity {
  def accountName: String
}

object Identity {

  case class AdminUser(accountName: String) extends Identity

  case class RegularUser(accountName: String) extends Identity

}
