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
import cats.implicits._
import io.circe.Encoder
import org.geolatte.featureserver.Domain._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import org.http4s.circe._
import org.http4s.server.Router

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class MetaEndpoints[F[_]: Sync](repo: Repository[F]) extends Http4sDsl[F] {

  private def toJsonOk[A](a: A)(implicit enc: Encoder[A]) = Ok(a.asJson)

  private def getSchemas(implicit enc: Encoder[Schema]) = {
    repo.listDatabases >>= (d => toJsonOk(d))
  }

  private def listTables(schema: String)(implicit enc: Encoder[Table]) =
    for {
      tables <- repo.listCollections(schema)
      resp   <- toJsonOk(tables)
    } yield resp

  private def apiv1: HttpRoutes[F] = {

    import Codecs.V1._
    HttpRoutes.of[F] {
      case GET -> Root / "databases"          => getSchemas
      case GET -> Root / "databases" / dbName => listTables(dbName)
    }
  }

  private def apiv2: HttpRoutes[F] = {
    import Codecs.V2._
    HttpRoutes.of[F] {
      case GET -> Root / "schemas"        => getSchemas
      case GET -> Root / "schemas" / name => listTables(name)
    }
  }

  def endpoints: HttpRoutes[F] =
    Router("/api" -> apiv1, "/api/v2" -> apiv2)
}

object MetaEndpoints {

  def endpoints[F[_]: Sync](repo: Repository[F]): HttpRoutes[F] = {
    new MetaEndpoints[F](repo).endpoints
  }
}
