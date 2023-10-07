package io.dockovpn.robot.admin.event

import io.kubernetes.client.openapi.ApiClient
import io.kubernetes.client.util.WebSockets
import okhttp3.WebSocket

import java.io.{InputStream, Reader}

class WebsocketListener(client: ApiClient) extends WebSockets.SocketListener {
  
  private var allBytes: Array[Byte] = Array.emptyByteArray
  
  override def open(protocol: String, socket: WebSocket): Unit = println("Websocket opened")
  
  override def bytesMessage(in: InputStream): Unit = {
    val bytes = in.readAllBytes()
    val len = bytes.length
    allBytes ++= bytes
    println(s"Bytes message received [$len] bytes")
  }
  
  override def textMessage(in: Reader): Unit = {
    import java.io.{BufferedReader, IOException}
    try {
      val reader = new BufferedReader(in)
      var line = reader.readLine
      while ( {
        line != null
      }) {
        println(line)
        
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
  
  override def close(): Unit = {
    val message = allBytes.map(_.toChar).mkString
    println(s"Session message: $message")
    client.getHttpClient.dispatcher.executorService.shutdown()
    println("Websocket closed")
  }
}
