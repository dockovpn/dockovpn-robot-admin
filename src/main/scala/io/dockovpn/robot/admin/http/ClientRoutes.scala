package io.dockovpn.robot.admin.http

import cats.data.Kleisli
import cats.effect.IO
import io.dockovpn.robot.admin.service.ClientService
import org.http4s.{HttpRoutes, Request, Response}
import org.http4s.dsl.io._
import org.http4s.circe.CirceEntityEncoder._

class ClientRoutes(clientService: ClientService) {
  
  val route: Kleisli[IO, Request[IO], Response[IO]] = HttpRoutes.of[IO] {
    case GET -> Root / "config" => for {
      result <- clientService.listClientConfigs
      response <- Ok(result)
    } yield response

    case GET -> Root / "config" / configName => for {
      result <- clientService.getClientConfig(configName)
      response <- Ok(result)
    } yield response
    
    case GET -> Root / "new" / "config" => for {
      result <- clientService.createConfig
      response <- Ok(result)
    } yield response
  }.orNotFound
}
