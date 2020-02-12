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

package org.geolatte.featureserver.config

import cats.effect.{Async, Blocker, ContextShift, Resource}
import doobie.hikari.HikariTransactor
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import scala.concurrent.ExecutionContext

/**
  * Application config
  * Created by Karel Maesen, Geovise BVBA on 2019-06-29.
  */
case class ServerConfig(host: String, port: Int)
case class DbConnectionsConfig(poolSize: Int)
case class DbConfig(url: String,
                    driver: String,
                    user: String,
                    password: String,
                    connections: DbConnectionsConfig)

object DbConfig {
  def dbTransactor[F[_]: Async: ContextShift](
      db: DbConfig,
      connEc: ExecutionContext,
      txnEc: Blocker): Resource[F, HikariTransactor[F]] = {
    HikariTransactor.newHikariTransactor[F](
      db.driver,
      db.url,
      db.user,
      db.password,
      connEc,
      txnEc
    )

  }
}

case class Config(server: ServerConfig, db: DbConfig)
