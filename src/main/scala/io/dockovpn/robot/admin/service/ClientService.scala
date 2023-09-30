package io.dockovpn.robot.admin.service

import cats.effect.{IO, Temporal}
import io.kubernetes.client.openapi.{ApiCallback, ApiException, Configuration}
import io.kubernetes.client.util.WebSockets
import okhttp3.WebSocket

import java.io.{InputStream, Reader}
import java.util
import scala.jdk.CollectionConverters._

class ClientService(watchNamespace: String, networkId: String) {
  
  import io.kubernetes.client.openapi.apis.CoreV1Api
  
  private val coreV1Api = new CoreV1Api
  
  def createConfig: IO[String] = getPods().flatMap { pods =>
    IO.blocking {
      val command = "./genclient.sh"
      val container = "dockovpn-container"
      
      val client = Configuration.getDefaultApiClient
      
      val call = coreV1Api.connectGetNamespacedPodExecCall(pods.head.getMetadata.getName, watchNamespace, command, container, true, null, true, true, new ApiCallback[String] {
        override def onFailure(e: ApiException, statusCode: Int, responseHeaders: util.Map[String, util.List[String]]): Unit = println("onFailure")
  
        override def onSuccess(result: String, statusCode: Int, responseHeaders: util.Map[String, util.List[String]]): Unit = println("onSuccess")
  
        override def onUploadProgress(bytesWritten: Long, contentLength: Long, done: Boolean): Unit = println("onUploadProgress")
  
        override def onDownloadProgress(bytesRead: Long, contentLength: Long, done: Boolean): Unit = println("onDownloadProgress")
      })
      WebSockets.stream(call, client, new WebSockets.SocketListener {
        override def open(protocol: String, socket: WebSocket): Unit = {}
  
        override def bytesMessage(in: InputStream): Unit = {}
  
        override def textMessage(in: Reader): Unit = {
          import java.io.BufferedReader
          import java.io.IOException
          try {
            val reader = new BufferedReader(in)
            var line = reader.readLine
            while ( {
              line != null
            }) {
              System.out.println(line)
      
              line = reader.readLine
            }
          } catch {
            case ex: IOException =>
              ex.printStackTrace()
          }
        }
  
        override def failure(t: Throwable): Unit = {
          t.printStackTrace()
          client.getHttpClient.dispatcher.executorService.shutdown()
        }
  
        override def close(): Unit = client.getHttpClient.dispatcher.executorService.shutdown()
      })
      Thread.sleep(5000)
      ""
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
