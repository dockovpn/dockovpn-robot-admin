ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "dockovpn-robot-admin",
    libraryDependencies ++= Seq(
      "io.fabric8" % "kubernetes-client" % "6.8.1",
      "org.bouncycastle" % "bcpkix-jdk15on" % "1.70"
    )
  )
