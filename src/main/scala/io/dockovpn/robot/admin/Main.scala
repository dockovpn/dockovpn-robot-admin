package io.dockovpn.robot.admin

import io.fabric8.kubernetes.client.KubernetesClientBuilder
import scala.jdk.CollectionConverters._

import java.util.Base64

object Main extends App {
  private val base64Decoder = Base64.getDecoder
  private val client = new KubernetesClientBuilder().build()
  private val items = client.secrets()
    .inNamespace("dockovpn")
    .withLabel("app", "dockovpn-config")
    .list().getItems.asScala
    .map(_.getData.asScala("config"))
    .map(config => base64Decoder.decode(config).map(_.toChar).mkString)
  
  println(items)
}
