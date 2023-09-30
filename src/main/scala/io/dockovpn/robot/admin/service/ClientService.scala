package io.dockovpn.robot.admin.service

import cats.effect.IO
import io.kubernetes.client.openapi.{ApiCallback, ApiException}

import java.util
import scala.jdk.CollectionConverters._

class ClientService(watchNamespace: String, networkId: String) {
  
  import io.kubernetes.client.openapi.apis.CoreV1Api
  
  private val coreV1Api = new CoreV1Api
  
  def createConfig: IO[String] = getPods().flatMap { pods =>
    IO.blocking {
      val command = "./genclient.sh"
      val container = "dockovpn-container"
      
      coreV1Api.connectGetNamespacedPodExec(pods.head.getMetadata.getName, watchNamespace, command, container, true, null, true, true)
    }
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
    
    coreV1Api
      .listNamespacedSecret(watchNamespace, null, null, null, null, labelSelector, null, null, null, null, false)
      .getItems.asScala.toList
  }
  
  private def getPods(extraLabels: Map[String, String] = Map.empty) = {
    IO.blocking {
      val labels = Map(
        "app" -> networkId
      ) ++ extraLabels
  
      val labelSelector = labels.map(p => p._1 + "=" + p._2).mkString(",")
  
      coreV1Api.listNamespacedPod(watchNamespace, null, null, null, null, labelSelector, null, null, null, null, false)
        .getItems.asScala.toList
    }
  }
}
