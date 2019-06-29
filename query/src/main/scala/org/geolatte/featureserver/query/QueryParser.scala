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

package org.geolatte.featureserver.query

import cats.effect._
import cats.implicits._
import org.parboiled2._
import org.geolatte.featureserver.QueryExpr._
import org.geolatte.featureserver.{Parser => FParser}

/**
  * A parser for simple Query expression
  *
  * Created by Karel Maesen, Geovise BVBA on 15/01/15.
  */
object DefaultParser extends FParser {

  override def parse[F[_]: Sync](s: String): F[Expr] = {
    val parser = new QueryParser(s)
    //parse input, and in case of ParseErrors format a nice message
    import Parser.DeliveryScheme.Throw
    Sync[F]
      .delay(parser.InputLine.run())
      .adaptError {
        case pe: ParseError =>
          new QueryParserException(
            parser.formatError(pe, new ErrorFormatter(showExpected = true, showPosition = true))
          )
      }
  }
}

class QueryParser(val input: ParserInput) extends Parser with StringBuilding {

  private val toValList: Atom => List[Atom] = v => List(v)
  private val combineVals: (List[Atom], Atom) => List[Atom] = (list, ve) => ve :: list
  private val toNum: String => LiteralNumber = (s: String) => LiteralNumber(BigDecimal(s))


  private val printableChar                  = CharPredicate.Printable -- "'"

  def InputLine: Rule1[Expr] = rule { BooleanExpression ~ EOI }

  def BooleanExpression: Rule1[Expr] = rule {
    BooleanTerm ~ WS ~ zeroOrMore(ignoreCase("or") ~ WS ~ BooleanTerm ~> BooleanOr)
  }

  def BooleanTerm: Rule1[Expr] = rule {
    BooleanFactor ~ WS ~ zeroOrMore(ignoreCase("and") ~ WS ~ BooleanFactor ~> BooleanAnd)
  }

  def MayBe: Rule1[Boolean] = rule {
    ignoreCase("is") ~ push(true) ~ WS ~ optional(ignoreCase("not") ~> ((_: Boolean) => false))
  }

  private def BooleanFactor: Rule1[Expr] = rule {
    WS ~ ignoreCase("not") ~ WS ~ BooleanPrim ~> BooleanNot | BooleanPrim
  }

  private def BooleanPrim = rule {
                                   (WS ~ ch('(') ~ WS ~ BooleanExpression ~ WS ~ ch(')') ~ WS) | Predicate
  }

  private def Predicate = rule {
    spatialRelPred | BetweenAndPred | ComparisonPred | ILikePred | InPred | LikePred | RegexPred | LiteralBool | isNullPred | JsonContainsPred
  }

  private def spatialRelPred = rule { intersectsTest }

  private def intersectsTest = rule {
    (WS ~ ignoreCase("intersects") ~ WS ~ GeomLiteral) ~> IntersectsExpr
  }

  private def GeomLiteral = rule {
    (ignoreCase("bbox") ~ push(None)) | LiteralStr ~> (s => Some(s.value))
  }

  private def isNullPred = rule {
    WS ~ AtomicExpression ~ WS ~ MayBe ~ WS ~ ignoreCase("null") ~> NullTestExpr
  }

  private def LikePred = rule {
    (WS ~ AtomicExpression ~ WS ~ ignoreCase("like") ~ WS ~ LikeR) ~> LikeExpr
  }

  private def ILikePred = rule {
    (WS ~ AtomicExpression ~ WS ~ ignoreCase("ilike") ~ WS ~ LikeR) ~> ILikeExpr
  }

  private def RegexPred = rule { (WS ~ AtomicExpression ~ WS ~ "~" ~ WS ~ RegexR) ~> RegexExpr }

  private def InPred = rule {
    (WS ~ AtomicExpression ~ WS ~ ignoreCase("in") ~ WS ~ ExpressionList ~ WS) ~> InExpr
  }

  private def BetweenAndPred = rule {
    (WS ~ AtomicExpression ~ WS ~ ignoreCase("between") ~ WS ~ AtomicExpression ~ WS ~ ignoreCase(
      "and"
    ) ~ WS ~ AtomicExpression ~ WS) ~> BetweenAndExpr
  }

  private def ComparisonPred = rule {
    (WS ~ AtomicExpression ~ WS ~ ComparisonOp ~ AtomicExpression ~ WS) ~> ComparisonExpr
  }

  private def ComparisonOp = rule {
    ">=" ~ push(GTE) | "<=" ~ push(LTE) | "=" ~ push(EQ) | "!=" ~ push(NEQ) | "<" ~ push(LT) | ">" ~ push(
      GT
    )
  }

  private def JsonContainsPred = rule {
    (WS ~ PropertyR ~ WS ~ ignoreCase("@>") ~ WS ~ LiteralStr ~ WS) ~> JsonContainsExpr
  }

  private def ExpressionList = rule {
    WS ~ "(" ~ WS ~ (AtomicExpression ~> toValList) ~ WS ~ zeroOrMore(
      "," ~ WS ~ (AtomicExpression ~> combineVals) ~ WS
    ) ~ WS ~ ")"
  }

  private def AtomicExpression = rule {
    FunctionApp | LiteralBool | LiteralStr | LiteralNum | PropertyR
  }

  private def FunctionApp = rule { ToDateApp }

  private def ToDateApp = rule {
    (WS ~ ignoreCase("to_date") ~ WS ~ "(" ~ (LiteralStr | PropertyR) ~ WS ~ "," ~ WS ~ LiteralStr ~ WS ~ ")" ~ WS) ~> ToDate
  }

  private def LiteralBool = rule {
    (ignoreCase("true") ~ push( LiteralBoolean( true ) )) | (ignoreCase( "false" ) ~ push(
      LiteralBoolean( false )
      ))
  }

  private def LiteralNum = rule { capture(Number) ~> toNum }

  //a Literal String is quoted using single quotes. To use singe quotes in the string, simply repeat the quote twice (with no whitespace).
  private def LiteralStr = rule {
    '\'' ~ clearSB() ~ zeroOrMore((printableChar | "''") ~ appendSB()) ~ '\'' ~ push(
      LiteralString(sb.toString)
    )
  }

  private def RegexR = rule {
    ch('/') ~ clearSB() ~ zeroOrMore(noneOf("/") ~ appendSB()) ~ ch('/') ~ push(
      Regex( sb.toString )
      )
  }

  private def LikeR = rule {
    '\'' ~ clearSB() ~ zeroOrMore((printableChar | "\'\'") ~ appendSB()) ~ '\'' ~ push(
      Like( sb.toString )
      )
  }

  private def PropertyR = rule { capture(NameString) ~> Property ~ WS }

  //basic tokens
  private def NameString = rule { !('\'' | ch('"')) ~ FirstNameChar ~ zeroOrMore(nonFirstNameChar) }

  private def FirstNameChar = rule { CharPredicate.Alpha | ch('_') }

  private def nonFirstNameChar = rule { CharPredicate.AlphaNum | ch('_') | ch('-') | ch('.') }

  private def Number = rule {
    optional('-') ~ WS ~ Digits ~ optional('.' ~ optional(Digits)) ~ optional(
      'E' ~ optional('-') ~ Digits
    )
  }

  private def Digits = rule { oneOrMore(CharPredicate.Digit) }

  private def WS = rule { zeroOrMore(" " | "\n" | "\t") }

  implicit def wspStr(s: String): Rule0 = rule {
    str(s) ~ zeroOrMore(' ')
  }

}
