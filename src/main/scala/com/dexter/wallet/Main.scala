package com.dexter.wallet

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main extends App {

  implicit val system: ActorSystem = ActorSystem("wallet-system")
  implicit val executionContext: ExecutionContextExecutor = system.getDispatcher

  val walletActor = system.classicSystem.actorOf(WalletActor.props)
  val walletService = new WalletService(walletActor)
  val walletRoutes = new WalletRoutes(walletService)

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(walletRoutes.routes)

  println(s"Server are running. Press RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}

