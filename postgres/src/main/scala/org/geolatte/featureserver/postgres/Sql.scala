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
import org.geolatte.featureserver.Domain._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
private[postgres] object Sql {

  def listDbs: Query0[Option[Schema]] =
    sql"""
    select distinct s.schema_name from information_schema.schemata s
    inner join information_schema.tables t on (s.schema_name = t.table_schema)
    where t.table_name = 'geolatte_nosql_collections';
    """.query[Option[Schema]]

  def listCollections(dbName: String): doobie.Query0[Option[Table]] =
    sql"""
         select distinct s.schema_name, t.table_name from information_schema.schemata s
         inner join information_schema.tables t on (s.schema_name = t.table_schema)
         where s.schema_name = $dbName and not (t.table_name ilike 'geolatte_%');
       """.query[Option[Table]]
}
