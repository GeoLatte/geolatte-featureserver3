package org.geolatte.featureserver.query

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-26.
  */
import org.geolatte.featureserver.QueryExpr._
import cats.effect.IO
import org.specs2.matcher._
import org.specs2._

/**
  * Created by Karel Maesen, Geovise BVBA on 15/01/15.
  */
class QueryParserSpec extends mutable.Specification with IOMatchers {

  "A QueryParser " should {

    //TODO -- check that the correct type of exception is returned as a Failure  condition.
    // this requires an extension of the current IOMatchers (contribute this directly?)

    "parse equality expressions correctly " in {
      DefaultParser.parse[IO]("var =  '123' ") must returnValue(
        ComparisonExpr(Property("var"), EQ, LiteralString("123"))
      )
    }

    "parse greater or equals expressions correctly " in {
      DefaultParser.parse[IO]("var >=  '123' ") must returnValue(
        ComparisonExpr(Property("var"), GTE, LiteralString("123"))
      )
    }

    "handle parentesis properly" in {
      DefaultParser.parse[IO]("( var =  '123' )") must returnValue(
        ComparisonExpr(Property("var"), EQ, LiteralString("123"))
      )
    }

    "Interpret literal numbers  properly" in {

      DefaultParser.parse[IO](" var =  -123 ") must returnValue(
        ComparisonExpr(Property("var"), EQ, LiteralNumber(BigDecimal(-123)))
      ) and (DefaultParser.parse[IO]("var =  123.54") must returnValue(
        ComparisonExpr(Property("var"), EQ, LiteralNumber(BigDecimal(123.54)))
      )) and
        (DefaultParser.parse[IO](" var =  123.54E3 ") must returnValue(
          ComparisonExpr(Property("var"), EQ, LiteralNumber(BigDecimal(123.54E3)))
        )) and (DefaultParser.parse[IO](" var =  123.54E-3") must returnValue(
        ComparisonExpr(Property("var"), EQ, LiteralNumber(BigDecimal(123.54E-3)))
      ))

    }

    "Interpret literals strings" in {

      (
        DefaultParser.parse[IO](" var =  'abc.d' ") must returnValue(
          ComparisonExpr(Property("var"), EQ, LiteralString("abc.d"))
        )
      ) and (
        DefaultParser.parse[IO]("var =  '!abc._-$%^&*()@#$%%^_+00==\"'") must returnValue(
          ComparisonExpr(Property("var"), EQ, LiteralString("!abc._-$%^&*()@#$%%^_+00==\""))
        )
      ) and (
        DefaultParser.parse[IO](" var =  'abc''def' ") must returnValue(
          ComparisonExpr(Property("var"), EQ, LiteralString("abc'def"))
        )
      ) and (
        DefaultParser.parse[IO](""" var =  '{"ab" : "def"}' """) must returnValue(
          ComparisonExpr(Property("var"), EQ, LiteralString("""{"ab" : "def"}"""))
        )
      )

    }

    "handle negations properly" in {

      (
        DefaultParser.parse[IO](" not (var =  '123')") must returnValue(
          BooleanNot(ComparisonExpr(Property("var"), EQ, LiteralString("123")))
        )
      ) and (
        DefaultParser.parse[IO](" not var =  '123'") must returnValue(
          BooleanNot(ComparisonExpr(Property("var"), EQ, LiteralString("123")))
        )
      )

    }

    "handle AND combinator properly" in {

      (
        DefaultParser.parse[IO]("(vara > 12) and not (varb =  '123')") must returnValue(
          BooleanAnd(
            ComparisonExpr(
              Property("vara"),
              GT,
              LiteralNumber(BigDecimal(12))
            ),
            BooleanNot(
              ComparisonExpr(Property("varb"), EQ, LiteralString("123"))
            )
          )
        )
      ) and (
        DefaultParser.parse[IO]("vara > 12 and varb =  '123'") must returnValue(
          BooleanAnd(
            ComparisonExpr(
              Property("vara"),
              GT,
              LiteralNumber(BigDecimal(12))
            ),
            ComparisonExpr(
              Property("varb"),
              EQ,
              LiteralString("123")
            )
          ))
      ) and (
        DefaultParser.parse[IO]("vara > 12 AND varb =  '123'") must returnValue(
          BooleanAnd(
            ComparisonExpr(
              Property("vara"),
              GT,
              LiteralNumber(BigDecimal(12))
            ),
            ComparisonExpr(
              Property("varb"),
              EQ,
              LiteralString("123")
            )
          ))
      )

    }

    "handle consecutive AND combinator without needing nesting parenthesis" in {
      DefaultParser.parse[IO]("vara > 12 and varb =  '123' and varc = 'bla'") must returnValue(
        BooleanAnd(
          BooleanAnd(
            ComparisonExpr(Property("vara"), GT, LiteralNumber(BigDecimal(12))),
            ComparisonExpr(Property("varb"), EQ, LiteralString("123"))
          ),
          ComparisonExpr(Property("varc"), EQ, LiteralString("bla"))
        )
      )
    }

    "handle OR combinator properly" in {
      (
        DefaultParser.parse[IO]("(vara > 12) or not (varb =  '123')") must returnValue(
          BooleanOr(
            ComparisonExpr(Property("vara"), GT, LiteralNumber(BigDecimal(12))),
            BooleanNot(ComparisonExpr(Property("varb"), EQ, LiteralString("123")))
          )
        )
      ) and (
        DefaultParser.parse[IO]("vara > 12 or varb =  '123'") must returnValue(
          BooleanOr(
            ComparisonExpr(Property("vara"), GT, LiteralNumber(BigDecimal(12))),
            ComparisonExpr(Property("varb"), EQ, LiteralString("123"))
          )
        )
      ) and (
        DefaultParser.parse[IO]("vara > 12 or ( varb =  '123' and varc = 'abc' ) ") must returnValue(
          BooleanOr(
            ComparisonExpr(Property("vara"), GT, LiteralNumber(BigDecimal(12))),
            BooleanAnd(
              ComparisonExpr(Property("varb"), EQ, LiteralString("123")),
              ComparisonExpr(Property("varc"), EQ, LiteralString("abc"))
            )
          )
        )
      )
    }

    "handle consecutive OR combinator without needing nesting parenthesis" in {
      DefaultParser.parse[IO]("vara > 12 or varb =  '123' or varc = 'bla'") must returnValue(
        BooleanOr(
          BooleanOr(
            ComparisonExpr(Property("vara"), GT, LiteralNumber(BigDecimal(12))),
            ComparisonExpr(Property("varb"), EQ, LiteralString("123"))
          ),
          ComparisonExpr(Property("varc"), EQ, LiteralString("bla"))
        )
      )
    }

    "handle boolean literal values in expression" in {
      (
        DefaultParser.parse[IO](" not (var =  true)") must returnValue(
          BooleanNot(ComparisonExpr(Property("var"), EQ, LiteralBoolean(true)))
        )
      ) and (
        DefaultParser.parse[IO](" not var =  false") must returnValue(
          BooleanNot(ComparisonExpr(Property("var"), EQ, LiteralBoolean(false)))
        )
      )
    }

    "treat a boolean literal as a valid expression" in {
      (
        DefaultParser.parse[IO]("true") must returnValue(
          LiteralBoolean(true)
        )
      ) and (
        DefaultParser.parse[IO](" not (false)") must returnValue(
          BooleanNot(LiteralBoolean(false))
        )
      )
    }

    "supports the in operator" in {
      (DefaultParser.parse[IO]("var in ('a', 'b', 'c')") must returnValue(
        InExpr(Property("var"),
               List(
                 LiteralString("c"),
                 LiteralString("b"),
                 LiteralString("a")
               ))
      )) and (DefaultParser.parse[IO]("var in  (  1,2, 3 )") must returnValue(
        InExpr(Property("var"),
               List(LiteralNumber(3),
                    LiteralNumber(2),
                    LiteralNumber(
                      1
                    )))
      ))
    }

    "support regex predicates" in {
      DefaultParser.parse[IO]("""var ~ /a\.*.*b/""") must returnValue(
        RegexExpr(Property("var"), Regex("""a\.*.*b"""))
      )
    }

    "support like predicate" in {
      DefaultParser.parse[IO]("""var like 'a%b' """) must returnValue(
        LikeExpr(Property("var"), Like("""a%b"""))
      )
    }

    "support ilike predicate" in {
      DefaultParser.parse[IO]("""var ilike 'a%b' """) must returnValue(
        ILikeExpr(Property("var"), Like("""a%b"""))
      )
    }

    "support the is null predicate" in {
      DefaultParser.parse[IO](""" var is null """) must returnValue(
        NullTestExpr(Property("var"), isNull = true)
      )
    }

    "support the is not null predicate" in {
      DefaultParser.parse[IO](""" var is NOT NULL """) must returnValue(
        NullTestExpr(Property("var"), isNull = false)
      )
    }

    "support the intersects predicate with bbox" in {
      DefaultParser.parse[IO](""" intersects bbox """) must returnValue(
        IntersectsExpr(None)
      )
    }

    "support the intersects predicate with WKT Expression" in {
      DefaultParser.parse[IO](""" intersects 'SRID=31370;POINT(1 1)' """) must returnValue(
        IntersectsExpr(Some("SRID=31370;POINT(1 1)"))
      )
    }

    "support the json contains predicate " in {
      DefaultParser.parse[IO](""" var @>  '["a",2]' """) must returnValue(
          JsonContainsExpr(Property("var"), LiteralString("""["a",2]"""))
      ) and (
        DefaultParser.parse[IO](""" var @>  '[''a'',2]' """) must returnValue(
          JsonContainsExpr(Property("var"), LiteralString("""['a',2]"""))
        )
      ) and (
        DefaultParser.parse[IO](""" var @> '{"a": "b"}' """) must returnValue(
          JsonContainsExpr(Property("var"), LiteralString("""{"a": "b"}"""))
        )
      )
    }

    "support the to_date function for strings" in {
      DefaultParser.parse[IO](""" var = to_date( '2019-04-30', 'YYYY-MM-DD')""") must returnValue(
        ComparisonExpr(
          Property("var"),
          EQ,
          ToDate(LiteralString("2019-04-30"), LiteralString("YYYY-MM-DD"))
        )
      )
    }

    "support the to_date function on properties" in {
      DefaultParser.parse[IO](""" var = to_date( a.b.c, 'YYYY-MM-DD')""") must returnValue(
        ComparisonExpr(
          Property("var"),
          EQ,
          ToDate(Property("a.b.c"), LiteralString("YYYY-MM-DD"))
        )
      )
    }

    "supports comparison on dates" in {
      DefaultParser.parse[IO](
        """ to_date( a.b.c, 'YYYY-MM-DD') = to_date('2019-04-30', 'YYYY-MM-DD') """) must returnValue(
        ComparisonExpr(
          ToDate(Property("a.b.c"), LiteralString("YYYY-MM-DD")),
          EQ,
          ToDate(LiteralString("2019-04-30"), LiteralString("YYYY-MM-DD"))
        )
      )
    }

    "support the between .. and ..  expression" in {
      DefaultParser.parse[IO](
        """ to_date( a.b.c, 'YYYY-MM-DD') between '2011-01-01' and '2012-01-01' """) must returnValue(
        BetweenAndExpr(
          ToDate(Property("a.b.c"), LiteralString("YYYY-MM-DD")),
          LiteralString("2011-01-01"),
          LiteralString("2012-01-01")
        )
      )
    }

  }

}
