package org.geolatte.featureserver

import cats.effect.Sync
import org.geolatte.featureserver.QueryExpr.BooleanExpr

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */

object QueryExpr {

  sealed trait Expr

  sealed trait BooleanExpr

  case class BooleanOr(lhs: BooleanExpr, rhs: BooleanExpr) extends BooleanExpr

  case class BooleanAnd(lhs: BooleanExpr, rhs: BooleanExpr) extends BooleanExpr

  case class BooleanNot(expr: BooleanExpr) extends BooleanExpr

  sealed trait Predicate extends BooleanExpr

  case class ComparisonPredicate(lhs: AtomicExpr, op: ComparisonOperator, rhs: AtomicExpr)
    extends Predicate

  case class InPredicate(lhs: AtomicExpr, rhs: ValueListExpr) extends Predicate

  case class RegexPredicate(lhs: AtomicExpr, rhs: RegexExpr) extends Predicate

  case class LikePredicate(lhs: AtomicExpr, rhs: LikeExpr) extends Predicate

  case class ILikePredicate(lhs: AtomicExpr, rhs: LikeExpr) extends Predicate

  case class NullTestPredicate(lhs: AtomicExpr, isNull: Boolean) extends Predicate

  case class IntersectsPredicate(wkt: Option[String]) extends Predicate {
    def intersectsWithBbox: Boolean = wkt.isDefined
  }

  case class JsonContainsPredicate(lhs: PropertyExpr, rhs: LiteralString) extends Predicate

  case class BetweenAndPredicate(date: AtomicExpr, lb: AtomicExpr, up: AtomicExpr) extends Predicate

  sealed trait AtomicExpr extends Expr

  case class LiteralString(value: String) extends AtomicExpr

  case class LiteralNumber(value: BigDecimal) extends AtomicExpr

  case class LiteralBoolean(value: Boolean) extends AtomicExpr with BooleanExpr

  case class PropertyExpr(path: String) extends AtomicExpr

  case class ToDate(date: AtomicExpr, fmt: AtomicExpr) extends AtomicExpr

  case class ValueListExpr(values: List[AtomicExpr]) extends Expr

  case class RegexExpr(pattern: String) extends Expr

  case class LikeExpr(pattern: String) extends Expr

  sealed trait ComparisonOperator

  case object EQ extends ComparisonOperator

  case object NEQ extends ComparisonOperator

  case object LT extends ComparisonOperator

  case object GT extends ComparisonOperator

  case object LTE extends ComparisonOperator

  case object GTE extends ComparisonOperator

  sealed trait Arg

  case class PropertyArg(propery: PropertyExpr) extends Arg

  case class ValueArg(value: AtomicExpr) extends Arg

  class QueryParserException(message: String = null) extends RuntimeException( message )

}

trait Parser {
  def parse[F[_]: Sync](s: String): F[BooleanExpr]
}

