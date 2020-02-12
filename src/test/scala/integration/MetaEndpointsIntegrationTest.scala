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

package integration

import cats.effect.IO
import io.circe.Json
import io.circe.config.{parser => Cparser}
import io.circe.literal._
import org.geolatte.featureserver.app.Server
import org.geolatte.featureserver.config.Config
import org.http4s._
import org.http4s.circe._
import org.http4s.client.{Client, JavaNetClientBuilder}
import org.http4s.client.blaze.BlazeClientBuilder
import org.specs2.execute.AsResult
import org.specs2.matcher.{ContainWithResult, Expectable, IOMatchers, ValueCheck}
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll

import scala.concurrent.ExecutionContext

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-07-10.
  */
//class MetaEndpointsIntegrationTest extends Specification with IOMatchers with BeforeAfterAll {
//
//  import com.typesafe.config.ConfigFactory
//
//  import scala.concurrent.ExecutionContext.Implicits.global
//  val config = Cparser.decodePath[Config](ConfigFactory.load("test"), "featureserver") match {
//    case Right(c)    => c
//    case Left(error) => sys.error(s"Failure to load configuration: $error")
//  }
//
//  import java.util.concurrent._
//
//  val blockingEC = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
//  val httpClient: Client[IO] = JavaNetClientBuilder[IO](blockingEC).create
//
//
//  val fiber = Server.createServer[IO](config).use(_ => IO.never).start.unsafeRunSync()
//
//  override def afterAll() = {
//    fiber.cancel.unsafeRunSync()
//  }
//
//  override def beforeAll() = {
//    //TODO load data in database
//  }
//
//  "Metadata service" should {
//    val metadataUrl: Uri = uri"http://localhost:8080/api/" / "databases"
//
//    BlazeClientBuilder[IO](global).resource.use { client =>
//      client.expect[Json](metadataUrl)
//    } must returnValue[Json](json"""[{"name" : "featureserver", "url" : "api/databases/featureserver"}]""" )
//
//  }
//
//}
