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

import org.geolatte.featureserver.QueryExpr.Expr
import fs2.Stream
import org.geolatte.featureserver.Domain._
import org.geolatte.geom._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
trait Repository[F[_]] {


  trait UpdateSpec {}

  trait View {}

  trait Index {}

  case class Metadata(
      name: String,
      envelope: Envelope[Position],
      level: Int,
      idType: String,
      count: Long = 0,
      geometryColumn: String = "GEOMETRY",
      pkey: String = "id",
      jsonTable: Boolean = true // is table defined by persistence Server? or registered
  )

  def createDb(dbname: String): F[Unit]

  def dropDb(dbname: String): F[Unit]

  def count(database: String, collection: String): F[Long]

  def metadata(database: String, collection: String, withCount: Boolean = false): F[Metadata]


  def existsCollection(dbName: String, colName: String): F[Boolean]

  def createCollection(dbName: String, colName: String, spatialSpec: Metadata): F[Boolean]

  def registerCollection(db: String, collection: String, metadata: Metadata): F[Boolean]

  def deleteCollection(dbName: String, colName: String): F[Boolean]


  def distinct[P <: Position](database: String,
                              collection: String,
                              spatialQuery: SpatialQuery,
                              projection: Projection): F[List[String]]

  def delete(database: String, collection: String, query: Expr): F[Boolean]

  def update(database: String,
             collection: String,
             query: Expr,
             updateSpec: UpdateSpec): F[Int]

  //def writer(database: String, collection: String): FeatureWriter

  /**
    * Saves a view for the specified database and collection.
    *
    * @param database the database for the view
    * @param collection the collection for the view
    * @param viewDef the view definition
    * @return eventually true if this save resulted in the update of an existing view, false otherwise
    */
  def saveView(database: String, collection: String, viewDef: View): F[Unit]

  def getViews(database: String, collection: String): F[List[View]]

  def getView(database: String, collection: String, id: String): F[View]

  def dropView(database: String, collection: String, id: String): F[Unit]

  def createIndex(dbName: String, colName: String, indexDef: Index): F[Unit]

  def getIndices(database: String, collection: String): F[List[Index]]

  def getIndex(database: String, collection: String, index: String): F[Index]

  def dropIndex(database: String, collection: String, index: String): F[Unit]

}
