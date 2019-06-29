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
object PGJsonQueryRenderer extends QueryRenderer {

  def renderPropertyExpr(expr: Property) = {
    val variadicPath = path2VariadicList(expr)
    s"json_extract_path(json, $variadicPath)"
  }

  def propertyPathAsJsonText(expr: Property): String = {
    val variadicPath = path2VariadicList(expr)
    s"json_extract_path_text(json, $variadicPath)"
  }

  def renderAtomicPropsAsText(expr: Atom): String = expr match {
    case p @ Property(_) => propertyPathAsJsonText(p)
    case _               => super.renderAtomic(expr)
  }

  override def renderAtomicCasting(lhs: Atom, rhs: Element): String = lhs match {
    case p @ Property(_) => s"${propertyPathAsJsonText(p)}${cast(rhs)}"
    case _               => s"${renderAtomic(lhs)}${cast(rhs)}"
  }

  override def renderNullTestPredicate(
      lhs: Atom,
      is: Boolean
  )(implicit ctxt: RenderContext): String =
    s" ${renderAtomicPropsAsText(lhs)} ${if (is) {
      "is"
    } else {
      "is not"
    }} null"

  override def renderToDate(
      date: Atom,
      fmt: Atom
  ): String = s" to_date(${renderAtomicPropsAsText(date)}, ${renderAtomicPropsAsText(fmt)}) "

  private def renderPropertyExprwithoutCast(lhs: Property): String = {
    val variadicPath: String = path2VariadicList(lhs)
    s"json_extract_path_text(json, $variadicPath)"
  }

  private def path2VariadicList(propertyExpr: Property): String =
    "'" + propertyExpr.path.replaceAll("\\.", "','") + "'"

}
