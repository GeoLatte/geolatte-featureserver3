/*
 * Copyright (c) 2019 Karel Maesen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
