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

import org.geolatte.featureserver.config._
import cats.effect.{Async, ConcurrentEffect, ContextShift, Resource, Timer}
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor
import fs2.Stream
import io.circe.config.parser
import org.geolatte.featureserver.MetaEndpoints
import org.geolatte.featureserver.config.Config
import org.geolatte.featureserver.postgres.PgRepository
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.{Router, Server => H4Server}
import org.http4s.server.middleware.Logger
import org.http4s.implicits._
import org.http4s.server.Router

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
object Server {
  
  def createServer[F[_]: ContextShift: ConcurrentEffect: Timer]: Resource[F, H4Server[F]] = {

    for {
      config <- Resource.liftF(parser.decodePathF[F, Config]("featureserver"))
      connEc <- ExecutionContexts.fixedThreadPool[F](config.db.connections.poolSize)
      txnEc  <- ExecutionContexts.cachedThreadPool[F]
      xa     <- DbConfig.dbTransactor(config.db, connEc, txnEc)
      repo         = new PgRepository[F](xa)
      httpApp      = MetaEndpoints.endpoints[F](repo).orNotFound
      finalHttpApp = Logger.httpApp(true, false)(httpApp)
      server <- BlazeServerBuilder[F]
        .bindHttp(config.server.port, config.server.host)
        .withHttpApp(finalHttpApp)
        .resource
    } yield server
  }

}
