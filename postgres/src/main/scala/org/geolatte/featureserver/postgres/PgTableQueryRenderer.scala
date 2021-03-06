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

import org.geolatte.featureserver.QueryExpr._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
object PgTableQueryRenderer extends QueryRenderer {

  override def renderPropertyExpr(expr: Property): String = {
    val withoutPrefix =
      if (expr.path.trim.startsWith("properties.")) {
        expr.path.trim.substring(11)
      } else {
        expr.path
      }
    s""""$withoutPrefix""""
  }

  override def cast(exp: Element): String = "" //don't cast

}
