package io.dockovpn.robot.admin.service

import cats.effect.{Deferred, IO}
import io.dockovpn.robot.admin.event.{ApiCallbackHandler, WebsocketListener}
import io.dockovpn.robot.admin.kubernetes.RichCoreV1Api
import io.kubernetes.client.openapi.Configuration
import io.kubernetes.client.util.WebSockets

import scala.jdk.CollectionConverters._

class ClientService(watchNamespace: String, networkId: String) {
  
  private val richCoreV1Api = new RichCoreV1Api
  
  def createConfig: IO[String] = getPods().flatMap { pods =>
    val commands = List("./genclient.sh", "o")
    val container = "dockovpn-container"
    
    val client = Configuration.getDefaultApiClient
    
    val call = richCoreV1Api.connectGetNamespacedPodExecCall(
      pods.head.getMetadata.getName,
      watchNamespace,
      commands,
      container,
      stderr = false,
      stdin = false,
      stdout = true,
      tty = false,
      ApiCallbackHandler.default
    )

    for {
      deferred <- Deferred[IO, String]
      _ <- IO(WebSockets.stream(call, client, new WebsocketListener(client, deferred))).start
      rez <- deferred.get
    } yield rez
  } handleErrorWith { t =>
    IO(t.printStackTrace()) >> IO.println(t.getMessage) >> IO.pure("")
  }
  
  def listClientConfigs: IO[List[String]] = {
    IO.blocking(
      getSecrets(Map("dockovpn-network-id" -> networkId))
        .map(_.getMetadata.getName)
    ).handleErrorWith { f =>
      IO.println(f.getMessage) >> IO.pure(List.empty)
    }
  }
  
  def getClientConfig(name: String): IO[String] = {
    IO.blocking(
      getSecrets()
      .filter(_.getMetadata.getName == name)
      .map(_.getData.asScala("config"))
      .map(_.map(_.toChar).mkString)
      .head
    ).handleErrorWith { f =>
      IO.println(f.getMessage) >> IO.pure("")
    }
  }
  
  private def getSecrets(extraLabels: Map[String, String] = Map.empty) = {
    val labels = Map(
      "app" -> "dockovpn-config"
    ) ++ extraLabels
    
    val labelSelector = labels.map(p => p._1 + "=" + p._2).mkString(",")
    
    richCoreV1Api
      .listNamespacedSecret(
        watchNamespace,
        null,
        null,
        null,
        null,
        labelSelector,
        null,
        null,
        null,
        null,
        false
      )
      .getItems.asScala.toList
  }
  
  private def getPods(extraLabels: Map[String, String] = Map.empty) = {
    IO.blocking {
      val labels = Map(
        "app" -> networkId
      ) ++ extraLabels
  
      val labelSelector = labels.map(p => p._1 + "=" + p._2).mkString(",")
  
      richCoreV1Api.listNamespacedPod(
        watchNamespace,
        null,
        null,
        null,
        null,
        labelSelector,
        null,
        null,
        null,
        null,
        false
      )
        .getItems.asScala.toList
    }
  }
}
