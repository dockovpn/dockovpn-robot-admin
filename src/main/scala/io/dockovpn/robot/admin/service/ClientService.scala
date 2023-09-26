package io.dockovpn.robot.admin.service

import io.fabric8.kubernetes.api.model.Secret
import io.fabric8.kubernetes.client.KubernetesClient

import java.util.Base64
import scala.jdk.CollectionConverters._

class ClientService(client: KubernetesClient, decoder: Base64.Decoder) {
  
  def listClientConfigs(networkId: String): List[String] = {
    val items = getSecrets(Map("dockovpn-network-id" -> networkId))
      .map(_.getMetadata.getName)
    
    items
  }
  
  def getClientConfig(name: String): String = {
    val items = getSecrets()
      .filter(_.getMetadata.getName == name)
      .map(_.getData.asScala("config"))
      .map(config => decoder.decode(config).map(_.toChar).mkString)
  
    items.head
  }
  
  private def getSecrets(extraLabels: Map[String, String] = Map.empty): List[Secret] = {
    val labels = Map(
      "app" -> "dockovpn-config"
    ) ++ extraLabels
    
    val items = client.secrets()
      .inNamespace("dockovpn")
      .withLabels(labels.asJava)
      .list().getItems.asScala
    
    items.toList
  }
}
