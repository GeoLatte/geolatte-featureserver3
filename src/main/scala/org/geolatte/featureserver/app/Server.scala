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

package org.geolatte.featureserver.app

import cats.effect.{Async, ConcurrentEffect, ContextShift, Timer}
import doobie.util.transactor.Transactor
import fs2.Stream
import org.geolatte.featureserver.MetaEndpoints
import org.geolatte.featureserver.postgres.PgRepository
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import org.http4s.implicits._
import org.http4s.server.Router

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
object Server {

  def getTransactor[F[_]: Async: ContextShift]: Transactor[F] =
    Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:9432/fserver",
      "fserver",
      ""
    )

  def stream[F[_]: ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    val xa           = getTransactor
    val repo         = new PgRepository[F](xa)
    val httpApp      = MetaEndpoints.endpoints[F]( repo ).orNotFound
    val finalHttpApp = Logger.httpApp(true, false)(httpApp)

    for {
      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain

}
