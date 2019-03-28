package org.geolatte.helloworld
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._

object Main extends IOApp {
  def run(args: List[String]) =
    HelloworldServer.stream[IO].compile.drain.as(ExitCode.Success)
}