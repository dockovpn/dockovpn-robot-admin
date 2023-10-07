package io.dockovpn.robot.admin.event

import io.kubernetes.client.openapi.{ApiCallback, ApiException}

import java.util

class ApiCallbackHandler extends ApiCallback[String] {
  override def onFailure(e: ApiException, statusCode: Int, responseHeaders: util.Map[String, util.List[String]]): Unit = println("onFailure")
  
  override def onSuccess(result: String, statusCode: Int, responseHeaders: util.Map[String, util.List[String]]): Unit = println("onSuccess")
  
  override def onUploadProgress(bytesWritten: Long, contentLength: Long, done: Boolean): Unit = println("onUploadProgress")
  
  override def onDownloadProgress(bytesRead: Long, contentLength: Long, done: Boolean): Unit = println("onDownloadProgress")
}

object ApiCallbackHandler {
  def default: ApiCallbackHandler = new ApiCallbackHandler
}
