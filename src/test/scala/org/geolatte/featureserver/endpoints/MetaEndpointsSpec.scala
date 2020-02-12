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

package org.geolatte.featureserver.endpoints

import cats.effect.IO
import io.circe.literal._
import org.geolatte.featureserver.Domain.{Schema, Table}
import org.geolatte.featureserver.{Domain, MetaEndpoints, MetadataRepository}
import org.http4s._
import org.http4s.circe._
import org.http4s.implicits._
import org.specs2.execute.Result
import org.specs2.matcher.Matchers
import org.specs2.mutable.Specification

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-07-10.
  */
class MetaEndpointsSpec extends Specification with Matchers {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val catsEffectTimer: cats.effect.Timer[IO] =
    IO.timer(global)

  implicit val catsEffectContextShift: cats.effect.ContextShift[IO] =
    IO.contextShift(global)

  val repo = new MetadataRepository[IO] {
    override def listSchemas: IO[List[Domain.Schema]] = IO.pure(List(Schema("testSchema")))

    override def listTables(dbname: String): IO[List[Domain.Table]] =
      IO.pure(List(Table(Schema("testSchema"), "testTAble")))
  }

  val response = MetaEndpoints.endpoints[IO](repo).orNotFound.run(
    Request(method=Method.GET, uri = uri"/api/databases")
  )

  def check[A](actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])(
      implicit ev: EntityDecoder[IO, A]
  ): Result = {
    val actualResp  = actual.unsafeRunSync
    val statusCheck = actualResp.status must_== expectedStatus
    val bodyCheck = expectedBody.fold[Boolean](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty
    )( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync must_== expected
    )
    statusCheck and bodyCheck
  }

  "metadata endpoint" should {
    val response = MetaEndpoints.endpoints[IO](repo).orNotFound.run(
      Request(method=Method.GET, uri = uri"/api/databases")
      )
     check(response, Status.Ok, Some(json"""[{"name":"testSchema", "url":"api/databases/testSchema"}]"""))
  }
}
