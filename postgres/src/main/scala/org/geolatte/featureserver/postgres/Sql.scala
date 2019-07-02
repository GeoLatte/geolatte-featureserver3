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

import doobie._
import doobie.implicits._
import cats.implicits._
import io.circe._
import org.geolatte.featureserver.Domain._
import org.geolatte.featureserver.SpatialQuery
import org.postgresql.util.PGobject

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
private[postgres] object Sql {

  implicit val logh = StatementLogger.handler

  //TODO -- We don't actually need to parse the geojson. See if we can not just pass it through
  implicit val JsonbMeta: doobie.Meta[Json] = doobie.Meta.Advanced
    .other[PGobject]("jsonb")
    .imap[Json](jsonStr => parser.parse(jsonStr.getValue).leftMap[Json](err => throw err).merge)(
      json => {
        val o = new PGobject
        o.setType("jsonb")
        o.setValue(json.noSpaces)
        o
      }
    )

  def listDbs: Query0[Option[Schema]] =
    sql"""
    select distinct s.schema_name from information_schema.schemata s
    inner join information_schema.tables t on (s.schema_name = t.table_schema)
    where t.table_name = 'geolatte_nosql_collections';
    """.query[Option[Schema]]

  def listCollections(schema: String): doobie.Query0[Option[Table]] =
    sql"""
         select distinct s.schema_name, t.table_name from information_schema.schemata s
         inner join information_schema.tables t on (s.schema_name = t.table_schema)
         where s.schema_name = $schema and not (t.table_name ilike 'geolatte_%');
       """.query[Option[Table]]

  // Since Postgres 11 we can also directly compose the geojson from a json table using:
  //select json::jsonb || json_build_object('geometry', st_asgeojson(geometry))::jsonb from scheam.table;
  def query(schema: String,
            table: String,
            spatialQuery: SpatialQuery,
            start: Option[Long],
            limit: Option[Long]) = {

    val selectFrom =
      fr"""select json::jsonb || json_build_object('geometry', st_asgeojson(geometry)::jsonb)::jsonb from"""

    val fromTable = Fragment.const(s"$schema.$table")
    val limitF = limit.map(lim => fr"LIMIT $lim").getOrElse(Fragment.empty)

    (selectFrom ++ fromTable ++ limitF).query[Json]
  }
}
