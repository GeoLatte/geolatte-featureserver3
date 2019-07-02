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

package org.geolatte.featureserver.postgres

import cats.effect._
import doobie.util.transactor.Transactor
import org.geolatte.featureserver.SpatialQuery
import org.specs2.mutable.Specification


/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */
object PgRepositoryTypeSpec extends Specification with doobie.specs2.IOChecker {

  override val  transactor: doobie.Transactor[IO] =  testTransactor

  check( Sql.listDbs )
  check( Sql.listCollections("test"))
  check( Sql.query("featureserver", "adviezen", SpatialQuery(), None, None))
  check( Sql.query("featureserver", "adviezen", SpatialQuery(), None, Some(10)))
}
