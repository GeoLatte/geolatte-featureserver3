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

case class RenderContext(geometryColumn: String, bbox: Option[String] = None)

/**
  * Created by Karel Maesen, Geovise BVBA on 16/04/16.
  *
  */
trait QueryRenderer {

  def renderPropertyExpr(exp: Property): String

  def renderAtomic(expr: Atom): String = expr match {
    case ToDate(date, fmt) => renderToDate(date, fmt)
    case LiteralBoolean(b) => if (b) " true " else " false "
    case LiteralNumber(n)  => s" ${n.toString} "
    case LiteralString(s)  => s" '$s' "
    case p @ Property(_)   => renderPropertyExpr(p)
  }

  def renderBetween(
      lhs: Atom,
      lb: Atom,
      up: Atom
  ): String = s" ( ${renderAtomic(lhs)} between ${renderAtomic(lb)} and ${renderAtomic(up)} ) "

  def renderComparison(
      lhs: Atom,
      op: ComparisonOperator,
      rhs: Atom
  )(implicit ctxt: RenderContext): String =
    s" ${renderAtomicCasting(lhs, rhs)} ${sym(op)} ( ${renderAtomic(rhs)} )"

  def renderInPredicate(
      lhs: Atom,
      rhs: List[Atom]
  )(implicit ctxt: RenderContext): String =
    s" ${renderAtomicCasting(lhs, rhs.head)} in (${rhs.map(renderAtomic(_) trim) mkString ","})"

  def renderRegexPredicate(
      lhs: Atom,
      rhs: Regex
  )(implicit ctxt: RenderContext): String = s" ${renderAtomicCasting(lhs, rhs)} ~ '${rhs.pattern}'"

  def renderLikePredicate(
      lhs: Atom,
      rhs: Like,
      caseSensitive: Boolean = true
  )(implicit ctxt: RenderContext): String =
    if (caseSensitive) s" ${renderAtomicCasting(lhs, rhs)} like '${rhs.pattern}'"
    else s" ${renderAtomicCasting(lhs, rhs)} ilike '${rhs.pattern}'"

  def renderNullTestPredicate(
      lhs: Atom,
      is: Boolean
  )(implicit ctxt: RenderContext): String =
    s" ${renderAtomic(lhs)} ${if (is) "is" else "is not"} null"

  def renderToDate(
      date: Atom,
      fmt: Atom
  ): String = s" to_date(${renderAtomic(date)}, ${renderAtomic(fmt)}) "

  def renderIntersects(wkt: Option[String],
                       geometryColumn: String,
                       bbox: Option[String]): String = {
    wkt match {
      case Some(geo) => s""" ST_Intersects( $geometryColumn, '$geo' )"""
      case _         => s""" ST_Intersects( $geometryColumn, '${bbox.getOrElse("POINT EMPTY")}' )"""
    }
  }

  def renderJsonContains(
      lhs: Property,
      rhs: LiteralString
  )(implicit ctxt: RenderContext): String =
    s"${renderPropertyExpr(lhs)}::jsonb @> '${rhs.value}'::jsonb "

  def render(expr: Expr)(implicit ctxt: RenderContext): String = expr match {
    case BooleanAnd(lhs, rhs)         => s" ( ${render(lhs)} ) AND ( ${render(rhs)} )"
    case BooleanOr(lhs, rhs)          => s" ( ${render(lhs)} ) OR ( ${render(rhs)} )"
    case BooleanNot(inner)            => s" NOT ( ${render(inner)} ) "
    case LiteralBoolean(b)            => if (b) " true " else " false "
    case ComparisonExpr(lhs, op, rhs) => renderComparison(lhs, op, rhs)
    case BetweenAndExpr(lhs, lb, up)  => renderBetween(lhs, lb, up)
    case InExpr(lhs, rhs)             => renderInPredicate(lhs, rhs)
    case RegexExpr(lhs, rhs)          => renderRegexPredicate(lhs, rhs)
    case LikeExpr(lhs, rhs)           => renderLikePredicate(lhs, rhs, caseSensitive = true)
    case ILikeExpr(lhs, rhs)          => renderLikePredicate(lhs, rhs, caseSensitive = false)
    case NullTestExpr(lhs, is)        => renderNullTestPredicate(lhs, is)
    case IntersectsExpr(wkt)          => renderIntersects(wkt, ctxt.geometryColumn, ctxt.bbox)
    case JsonContainsExpr(lhs, rhs)   => renderJsonContains(lhs, rhs)
  }

  def cast(exp: Element): String = exp match {
    case LiteralBoolean(_) => "::bool"
    case LiteralNumber(_)  => "::decimal"
    case LiteralString(_)  => "::text"
    case Regex(_)          => "::text"
    case Like(_)           => "::text"
    case _                 => ""
  }

  def renderAtomicCasting(lhs: Atom, castType: Element): String = lhs match {
    case p @ Property(_) => s"${renderPropertyExpr(p)}${cast(castType)}"
    case _               => s"${renderAtomic(lhs)}${cast(castType)}"
  }

  def sym(op: ComparisonOperator): String = op match {
    case EQ  => " = "
    case NEQ => " != "
    case LT  => " < "
    case GT  => " > "
    case LTE => " <= "
    case GTE => " >= "
  }

}
