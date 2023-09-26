ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

val http4sVersion = "0.23.23"

lazy val root = (project in file("."))
  .settings(
    name := "dockovpn-robot-admin",
    libraryDependencies ++= Seq(
      "io.fabric8" % "kubernetes-client" % "6.8.1",
      "org.bouncycastle" % "bcpkix-jdk15on" % "1.70",
      "org.typelevel" %% "cats-effect" % "3.5.1" withSources() withJavadoc(),
      "co.fs2" %% "fs2-core" % "3.9.1" withSources() withJavadoc(),
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-literal" % "0.14.6"
    )
  )
