package org.geolatte.featureserver

import cats.effect.Sync

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */

object QueryExpr {

  sealed trait Element
  sealed trait Expr extends Element
  sealed trait Atom extends Element

  case class LiteralBoolean(v: Boolean) extends Expr with Atom

  case class BooleanOr(lhs: Expr, rhs: Expr) extends Expr

  case class BooleanAnd(lhs: Expr, rhs: Expr) extends Expr

  case class BooleanNot(expr: Expr) extends Expr

  case class ComparisonExpr(lhs:Atom, op: ComparisonOperator, rhs: Atom)  extends Expr

  case class InExpr(lhs: Atom, rhs: List[Atom]) extends Expr

  case class RegexExpr(lhs: Atom, rhs: Regex) extends Expr

  case class LikeExpr(lhs: Atom, rhs: Like) extends Expr

  case class ILikeExpr(lhs: Atom, rhs: Like) extends Expr

  case class NullTestExpr(lhs: Atom, isNull: Boolean) extends Expr

  case class IntersectsExpr(wkt: Option[String]) extends Expr {
    def intersectsWithBbox: Boolean = wkt.isDefined
  }

  case class JsonContainsExpr(lhs: Property, rhs: LiteralString) extends Expr

  case class BetweenAndExpr(date: Atom, lb: Atom, up: Atom) extends Expr


  case class LiteralString(value: String) extends Atom

  case class LiteralNumber(value: BigDecimal) extends Atom

  case class Property(path: String) extends Atom

  case class ToDate(date: Atom, fmt: LiteralString) extends Atom

  case class Regex(pattern: String) extends Element

  case class Like(pattern: String) extends Element

  sealed trait ComparisonOperator extends Element

  case object EQ extends ComparisonOperator

  case object NEQ extends ComparisonOperator

  case object LT extends ComparisonOperator

  case object GT extends ComparisonOperator

  case object LTE extends ComparisonOperator

  case object GTE extends ComparisonOperator


  class QueryParserException(message: String = null) extends RuntimeException( message )

}

trait Parser {
  def parse[F[_]: Sync](s: String): F[QueryExpr.Expr]
}

