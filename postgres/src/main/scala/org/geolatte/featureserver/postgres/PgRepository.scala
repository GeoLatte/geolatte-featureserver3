package org.geolatte.featureserver.postgres

import cats.effect.{Bracket, Sync}
import cats.implicits._
import doobie._
import doobie.implicits._
import org.geolatte.featureserver.Domain._
import org.geolatte.featureserver.{QueryExpr, Repository}
import org.geolatte.geom.types.Position

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
class PgRepository[F[_]: Bracket[?[_], Throwable]](val xa: Transactor[F]) extends Repository[F] {

  override def listDatabases: F[List[Database]] =
    Sql.listDbs.stream.collect { case Some(s) => s }.compile.toList.transact(xa)

  override def createDb(dbname: String): F[Unit] = ???

  override def dropDb(dbname: String): F[Unit] = ???

  override def count(database: String, collection: String): F[Long] = ???

  override def metadata(database: String, collection: String, withCount: Boolean): F[Metadata] = ???

  override def listCollections(dbname: String): F[List[String]] = ???

  override def existsCollection(dbName: String, colName: String): F[Boolean] = ???

  override def createCollection(dbName: String,
                                colName: String,
                                spatialSpec: Metadata): F[Boolean] = ???

  override def registerCollection(db: String, collection: String, metadata: Metadata): F[Boolean] =
    ???

  override def deleteCollection(dbName: String, colName: String): F[Boolean] = ???

  override def query(database: String,
                     collection: String,
                     spatialQuery: SpatialQuery,
                     start: Option[Int],
                     limit: Option[Int]): fs2.Stream[F, Feature] = ???

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
    * Saves a view for the specified database and collection.
    *
    * @param database the database for the view
    * @param collection the collection for the view
    * @param viewDef the view definition
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
