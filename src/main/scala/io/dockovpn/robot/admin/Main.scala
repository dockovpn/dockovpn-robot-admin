package io.dockovpn.robot.admin

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.IpLiteralSyntax
import io.dockovpn.robot.admin.http.ClientRoutes
import io.dockovpn.robot.admin.service.ClientService
import io.fabric8.kubernetes.client.KubernetesClientBuilder
import org.http4s.ember.server.EmberServerBuilder

import java.util.Base64

object Main extends IOApp {
  private val base64Decoder = Base64.getDecoder
  private val client = new KubernetesClientBuilder().build()
  private val clientService = new ClientService(client, base64Decoder)
  private val clientRoutes = new ClientRoutes(clientService)
  
  override def run(args: List[String]): IO[ExitCode] =
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(clientRoutes.route)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
  
}
