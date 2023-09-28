package io.dockovpn.robot.admin.service

import cats.effect.IO
import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.client.KubernetesClient

import java.util.Base64
import scala.jdk.CollectionConverters._

class ClientService(client: KubernetesClient, decoder: Base64.Decoder, watchNamespace: String, networkId: String) {
  
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
      .map(config => decoder.decode(config).map(_.toChar).mkString)
      .head
    ).handleErrorWith { f =>
      IO.println(f.getMessage) >> IO.pure("")
    }
  }
  
  private def getSecrets(extraLabels: Map[String, String] = Map.empty): List[Secret] = {
    val labels = Map(
      "app" -> "dockovpn-config"
    ) ++ extraLabels
    
    val items = client.secrets()
      .inNamespace(watchNamespace)
      .withLabels(labels.asJava)
      .list().getItems.asScala
    
    items.toList
  }
}
