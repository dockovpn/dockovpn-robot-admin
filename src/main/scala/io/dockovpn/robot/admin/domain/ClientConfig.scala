package io.dockovpn.robot.admin.domain

case class ClientConfig(data: String) {
  def clientId: String = data.split("\n").last.split(" ").last
}
