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
import io.circe.{Encoder, Json}
import org.geolatte.featureserver.Domain._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._

import org.http4s.circe._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class MetaEndpoints[F[_]: Sync] extends Http4sDsl[F] {

  implicit val encodeDb: Encoder[Database] = new Encoder[Database] {
    final def apply(a: Database): Json = Json.obj(
      ("name", Json.fromString(a.name)),
      ("url", Json.fromString((Root / "databases" / a.name).toString()))
    )
  }

  implicit val encodeCollection: Encoder[Collection] = new Encoder[Collection] {
    final def apply(a: Collection): Json = Json.obj(
      ("name", Json.fromString(a.name)),
      ("url", Json.fromString((Root / "databases" / a.db.name / a.name).toString()))
    )
  }

  private def getDbs(repo: Repository[F]): HttpRoutes[F] = {

    HttpRoutes.of[F] {

      case GET -> Root / "api" / "databases" =>
        for {
          databases <- repo.listDatabases
          resp      <- Ok(databases.asJson)
        } yield resp

    }
  }

  private def getDb(repo: Repository[F]): HttpRoutes[F] = {
    HttpRoutes.of[F] {

      case GET -> Root / "api" / "databases" / dbName =>
        for {
          collections <- repo.listCollections(dbName)
          resp        <- Ok(collections.asJson)
        } yield resp

    }
  }

  def endpoints(repo: Repository[F]): HttpRoutes[F] = getDbs(repo) <+> getDb(repo)
}

object MetaEndpoints {

  def endpoints[F[_]: Sync](repo: Repository[F]): HttpRoutes[F] = {
    new MetaEndpoints[F].endpoints(repo)
  }
}
