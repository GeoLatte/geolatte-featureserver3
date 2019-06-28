package org.geolatte.featureserver.postgres

import doobie._
import doobie.implicits._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
private[postgres] object Sql {

  def listDbs: Query0[Option[String]] =
    sql"""
    select distinct s.schema_name from information_schema.schemata s
    inner join information_schema.tables t on (s.schema_name = t.table_schema)
    where t.table_name = 'geolatte_nosql_collections';
    """.query[Option[String]]

}
