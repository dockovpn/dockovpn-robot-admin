package io.dockovpn.robot.admin.event

case class WebsocketSessionResult(
  textMessages: List[String],
  byteMessages: List[Array[Byte]],
  errors: List[Throwable]
) {
  val successful: Boolean = errors.isEmpty
}
