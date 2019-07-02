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

import fs2.Stream
import io.circe.Json
import org.geolatte.featureserver.QueryExpr.Expr
import org.geolatte.geom.types._

trait Projection {}

trait SortSpec {}

case class SpatialQuery(
    windowOpt: Option[Envelope[Position]] = None,
    intersectionGeometryWktOpt: Option[String] = None,
    queryOpt: Option[Expr] = None,
    projection: Option[Projection] = None,
    sort: List[SortSpec] = List(),
    withCount: Boolean = false,
    explode: Boolean = false
)

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-07-02.
  */
trait QueryRepository[F[_]] {

  def queryList(schema: String,
            table: String,
            spatialQuery: SpatialQuery,
            start: Option[Long] = None,
            limit: Option[Long] = None):  F[List[Json]]

  def queryStream(schema: String,
                table: String,
                spatialQuery: SpatialQuery,
                start: Option[Long] = None,
                limit: Option[Long] = None):  Stream[F, String]

}
