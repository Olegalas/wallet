name := "wallet"
version := "0.1"
scalaVersion := "2.13.0"

val AkkaVersion = "2.7.0"
val AkkaHttpVersion = "10.4.0"
val circeVersion = "0.14.1"
val heikoseebergerVersion = "1.39.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "de.heikoseeberger" %% "akka-http-circe" % heikoseebergerVersion
)
