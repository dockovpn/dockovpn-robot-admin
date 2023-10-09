package io.dockovpn.robot.admin.service

import cats.effect.{Deferred, IO}
import io.dockovpn.robot.admin.domain.ClientConfig
import io.dockovpn.robot.admin.event.{ApiCallbackHandler, WebsocketSessionListener}
import io.dockovpn.robot.admin.kubernetes.RichCoreV1Api
import io.kubernetes.client.openapi.models.{V1ObjectMeta, V1Pod, V1Secret}
import io.kubernetes.client.openapi.{ApiException, Configuration}
import io.kubernetes.client.util.WebSockets
import okhttp3.Call

import scala.jdk.CollectionConverters._
import scala.util.Random

class ClientService(watchNamespace: String, networkId: String) {
  
  private val richCoreV1Api = new RichCoreV1Api
  
  def createConfig: IO[String] = (for {
    execCall <- makePodExecCall(containerName = "dockovpn-container", commands = List("./genclient.sh", "o"))
    deferred <- Deferred[IO, String]
    _ <- streamPodExecCall(execCall, deferred).start
    rez <- deferred.get
    _ <- createSecretFromConfig(ClientConfig(rez))
  } yield rez).handleErrorWith {
    case aex: ApiException => IO.println(aex.printStackTrace()) >> IO.pure(aex.getResponseBody)
    case x => IO.println(x.printStackTrace()) >> IO.pure(x.getMessage)
  }
  
  def listClientConfigs: IO[List[String]] = {
    IO.blocking(
      getSecrets(Map("dockovpn-network-id" -> networkId))
        .map(_.getMetadata.getName)
    ).handleErrorWith {
      case aex: ApiException => IO.println(aex.printStackTrace()) >> IO.pure(List(aex.getResponseBody))
      case x => IO.println(x.printStackTrace()) >> IO.pure(List(x.getMessage))
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
  
  private def createSecretFromConfig(clientConfig: ClientConfig): IO[V1Secret] = IO.blocking {
    val metaData = new V1ObjectMeta()
    val suffix = Random.nextInt(100000)
    metaData.setName(s"$networkId-cfg-$suffix")
    metaData.setLabels(Map(
      "app" -> "dockovpn-config",
      "dockovpn-network-id" -> networkId,
      "dockovpn-config-name" -> clientConfig.clientId
    ).asJava)
    
    val data = Map("config" -> clientConfig.data)
    
    val secret = new V1Secret()
    secret.setType("Opaque")
    secret.setStringData(data.asJava)
    secret.setMetadata(metaData)
    
    richCoreV1Api.createNamespacedSecret(
      watchNamespace,
      secret,
      null,
      null,
      null,
      null
    )
  }
  
  private def streamPodExecCall(call: Call, callback: Deferred[IO, String]): IO[Unit] = IO {
    val client = Configuration.getDefaultApiClient
    
    WebSockets.stream(
      call,
      client,
      new WebsocketSessionListener(client, callback)
    )
  }
  
  private def makePodExecCall(containerName: String, commands: List[String]): IO[Call] = for {
    podName <- getDockovpnCorePod().map(_.getMetadata.getName)
    call <- IO {
      richCoreV1Api.connectGetNamespacedPodExecCall(
        podName,
        watchNamespace,
        commands,
        containerName,
        stderr = false,
        stdin = false,
        stdout = true,
        tty = false,
        ApiCallbackHandler.default
      )
    }
  } yield call
  
  private def getSecrets(extraLabels: Map[String, String] = Map.empty): List[V1Secret] = {
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
  
  private def getDockovpnCorePod(): IO[V1Pod] = getPods().map(_.head)
  
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
