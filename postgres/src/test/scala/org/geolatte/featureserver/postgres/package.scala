package org.geolatte.featureserver

import cats.effect.{Async, ContextShift, Effect, IO}
import doobie._
import doobie.implicits._
import doobie.util.transactor.Transactor

import scala.concurrent.ExecutionContext

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
package object postgres {

  def getTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:9432/fserver",
      "fserver",
      ""
    )

  /*
   * Provide a transactor for testing
   *  -- effectful because later we need to inject the configuration
   */
  def initializedTransactor[F[_]: Effect: Async: ContextShift]: F[Transactor[F]] =
    Async[F].delay( getTransactor )

  lazy val testEc: ExecutionContext = ExecutionContext.Implicits.global

  implicit lazy val testCs: ContextShift[IO] = IO.contextShift( testEc )

  lazy val testTransactor: Transactor[IO] = initializedTransactor[IO].unsafeRunSync()
}
