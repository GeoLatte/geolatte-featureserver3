package org.geolatte.featureserver

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */
object Domain {

  case class Database(name: String)

  case class Collection(db: Database, name: String)

}
