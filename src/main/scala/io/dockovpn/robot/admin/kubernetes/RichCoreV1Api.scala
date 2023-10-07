package io.dockovpn.robot.admin.kubernetes

import io.kubernetes.client.openapi.{ApiCallback, Configuration, Pair}
import io.kubernetes.client.openapi.apis.CoreV1Api
import okhttp3.Call

import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
 * Useful overloaded methods from CoreV1Api
 */
class RichCoreV1Api extends CoreV1Api {
  
  private val apiClient = Configuration.getDefaultApiClient
  
  /**
   * This method allows to pass list of commands
   * @param name
   * @param namespace
   * @param commands
   * @param container
   * @param stderr
   * @param stdin
   * @param stdout
   * @param tty
   * @param _callback
   * @return
   */
  def connectGetNamespacedPodExecCall(name: String, namespace: String, commands: List[String], container: String, stderr: Boolean, stdin: Boolean, stdout: Boolean, tty: Boolean, _callback: ApiCallback[_]): Call = {
    val postBody = null
    val path = s"/api/v1/namespaces/${apiClient.escapeString(namespace)}/pods/${apiClient.escapeString(name)}/exec"
    var queryParams = List.empty[Pair]
    var collectionQueryParams = List.empty[Pair]
    
    if (commands != null && commands.nonEmpty) collectionQueryParams ++= commands.flatMap { command =>
      apiClient.parameterToPair("command", command).asScala
    }
    if (container != null) queryParams ++= apiClient.parameterToPair("container", container).asScala
    if (stderr) queryParams ++= apiClient.parameterToPair("stderr", stderr).asScala
    if (stdin) queryParams ++= apiClient.parameterToPair("stdin", stdin).asScala
    if (stdout) queryParams ++= apiClient.parameterToPair("stdout", stdout).asScala
    if (tty) queryParams ++= apiClient.parameterToPair("tty", tty).asScala
    
    val headerParams = mutable.Map.empty[String, String]
    val cookieParams = mutable.Map.empty[String, String]
    val formParams = mutable.Map.empty[String, AnyRef]
    val accept = apiClient.selectHeaderAccept(Array("*/*"))
    if (accept != null) headerParams.put("Accept", accept)
    val contentType = apiClient.selectHeaderContentType(Array.empty[String])
    headerParams.put("Content-Type", contentType)
    val authNames = Array[String]("BearerToken")
    
    apiClient.buildCall(
      path,
      "GET",
      queryParams.asJava,
      collectionQueryParams.asJava,
      postBody,
      headerParams.asJava,
      cookieParams.asJava,
      formParams.asJava,
      authNames,
      _callback
    )
  }
}
