package io.dockovpn.robot.admin.service

import cats.effect.IO

import java.util.Base64
import scala.jdk.CollectionConverters._

class ClientService(decoder: Base64.Decoder, watchNamespace: String, networkId: String) {
  
  import io.kubernetes.client.openapi.apis.CoreV1Api
  
  private val client = new CoreV1Api
  
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
    
    client
      .listNamespacedSecret(watchNamespace, null, null, null, null, labelSelector, null, null, null, null, false)
      .getItems.asScala.toList
  }
}
