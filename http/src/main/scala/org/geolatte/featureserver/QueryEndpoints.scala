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

import cats.effect.Sync
import cats._
import cats.implicits._
import io.circe.Json
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe._ //required for derivation of proper entity encoder

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-07-02.
  */
class QueryEndpoints[F[_]: Sync](repo: QueryRepository[F]) extends Http4sDsl[F] {

  private def query(schema: String, table: String) =
    Ok {
      repo.query(schema, table, SpatialQuery(), limit = Some(10))
    }

  private def apiv1: HttpRoutes[F] = {

    import Codecs.V1._
    HttpRoutes.of[F] {
      case GET -> Root / "databases" / dbName / collection / "query" => query(dbName, collection)
    }
  }

  def endpoints: HttpRoutes[F] = Router("/api" -> apiv1)
}

object QueryEndpoints {
  def endpoints[F[_]: Sync](repo: QueryRepository[F]): HttpRoutes[F] = {
    new QueryEndpoints[F](repo).endpoints
  }
}
