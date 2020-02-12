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

import cats.effect.Bracket
import doobie._
import doobie.implicits._
import fs2.Stream
import io.circe.Json
import org.geolatte.featureserver
import org.geolatte.featureserver._
import org.geolatte.featureserver.Domain._
import org.geolatte.geom.Position

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class PgRepository[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F])
    extends Repository[F]
    with MetadataRepository[F]
    with QueryRepository[F] {

  override def listSchemas: F[List[Schema]] =
    Sql.listDbs.stream.collect { case Some(s) => s }.compile.toList.transact(xa)

  override def listTables(dbName: String): F[List[Table]] =
    Sql.listCollections(dbName).stream.collect { case Some(s) => s }.compile.toList.transact(xa)

  override def queryList(schema: String,
                         table: String,
                         spatialQuery: SpatialQuery,
                         start: Option[Long],
                         limit: Option[Long]): F[List[Json]] =
    Sql
      .query(schema, table, spatialQuery, start, limit)
      .to[List]
      .transact(xa)

  override def queryStream(schema: String,
                           table: String,
                           spatialQuery: SpatialQuery,
                           start: Option[Long],
                           limit: Option[Long]): Stream[F, String] =
    Sql
      .query(schema, table, spatialQuery, start, limit)
      .stream
      .map(json => json.noSpaces)
      .intersperse("\n")
      .transact(xa)

  override def existsCollection(dbName: String, colName: String): F[Boolean] = ???

  override def createCollection(dbName: String,
                                colName: String,
                                spatialSpec: Metadata): F[Boolean] = ???

  override def registerCollection(db: String, collection: String, metadata: Metadata): F[Boolean] =
    ???

  override def deleteCollection(dbName: String, colName: String): F[Boolean] = ???

  override def createDb(dbname: String): F[Unit] = ???

  override def dropDb(dbname: String): F[Unit] = ???

  override def count(database: String, collection: String): F[Long] = ???

  override def metadata(database: String, collection: String, withCount: Boolean): F[Metadata] = ???

  override def distinct[P <: Position](database: String,
                                       collection: String,
                                       spatialQuery: SpatialQuery,
                                       projection: Projection): F[List[String]] = ???

  override def delete(database: String, collection: String, query: QueryExpr.Expr): F[Boolean] = ???

  override def update(database: String,
                      collection: String,
                      query: QueryExpr.Expr,
                      updateSpec: UpdateSpec): F[Int] = ???

  /**
    * Saves a view for the specified schema and table.
    *
    * @param database   the schema for the view
    * @param collection the table for the view
    * @param viewDef    the view definition
    * @return eventually true if this save resulted in the update of an existing view, false otherwise
    */
  override def saveView(database: String, collection: String, viewDef: View): F[Unit] = ???

  override def getViews(database: String, collection: String): F[List[View]] = ???

  override def getView(database: String, collection: String, id: String): F[View] = ???

  override def dropView(database: String, collection: String, id: String): F[Unit] = ???

  override def createIndex(dbName: String, colName: String, indexDef: Index): F[Unit] = ???

  override def getIndices(database: String, collection: String): F[List[Index]] = ???

  override def getIndex(database: String, collection: String, index: String): F[Index] = ???

  override def dropIndex(database: String, collection: String, index: String): F[Unit] = ???

}
