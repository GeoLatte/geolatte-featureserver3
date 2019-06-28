package org.geolatte.featureserver.postgres

import cats.effect.IO
import org.geolatte.featureserver.query.DefaultParser
import org.specs2.matcher.IOMatchers
import org.specs2.mutable.Specification

/**
  * Created by Karel Maesen, Geovise BVBA on 23/01/15.
  */
class QueryRenderSpec extends Specification with IOMatchers {

  private implicit val renderContext: RenderContext =
    RenderContext("geometry", Some("SRID=31370;POLYGON((1 1,100 1,100 100,1 100,1 1))"))

  "The PGJsonQueryRenderer " should {

    val renderer = PGJsonQueryRenderer

    "properly render boolean expressions containing equality expresssions " in {
      DefaultParser
        .parse[IO]("ab.cd = 12")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'ab','cd')::decimal = ( 12 )")
    }

    "properly render comparison expressions contain boolean literal" in {
      DefaultParser
        .parse[IO]("ab.cd = TRUE")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'ab','cd')::bool = ( true )")
    }

    "properly render simple equality expression " in {
      DefaultParser
        .parse[IO]("properties.foo = 'bar1'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo')::text = ( 'bar1' )")
    }

    "properly render boolean expressions containing comparison expresssions other than equality " in {
      (
        DefaultParser
          .parse[IO]("ab.cd > 12")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(
          "json_extract_path_text(json, 'ab','cd')::decimal > ( 12 )")
      ) and (
        DefaultParser
          .parse[IO]("ab.cd >= 12")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(
          "json_extract_path_text(json, 'ab','cd')::decimal >= ( 12 )")
      ) and (
        DefaultParser
          .parse[IO]("ab.cd != 'abc'")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(
          "json_extract_path_text(json, 'ab','cd')::text != ( 'abc' )")
      )
    }

    "properly render negated expressions " in {
      DefaultParser
        .parse[IO]("not ab.cd = 12")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "NOT ( json_extract_path_text(json, 'ab','cd')::decimal = ( 12 ) )")
    }

    "properly render AND expressions " in {
      DefaultParser
        .parse[IO](" (ab = 12) and (cd > 'c') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "( json_extract_path_text(json, 'ab')::decimal = ( 12 ) ) AND ( json_extract_path_text(json, 'cd')::text > ( 'c' ) )")
    }

    "properly render OR expressions " in {
      DefaultParser
        .parse[IO](" (ab = 12) or (cd > 'c') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "( json_extract_path_text(json, 'ab')::decimal = ( 12 ) ) OR ( json_extract_path_text(json, 'cd')::text > ( 'c' ) )")
    }

    "properly render a boolean literal as a boolean expression" in {
      DefaultParser.parse[IO]("TRUE").map(expr => renderer.render(expr)) must returnValue(" true ")
    }

    "property render an IN predicate expression" in {
      DefaultParser
        .parse[IO](" a.b in (1,2,3) ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'a','b')::decimal in (3,2,1)")
    }

    "properly render simple regex expression " in {
      DefaultParser
        .parse[IO]("properties.foo ~ /bar1.*/")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo')::text ~ 'bar1.*'")
    }

    "properly render simple like expression " in {
      DefaultParser
        .parse[IO]("properties.foo like 'a%bcd'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo')::text like 'a%bcd'")
    }

    "properly render simple like expression " in {
      DefaultParser
        .parse[IO]("properties.foo ilike 'a%bcd'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo')::text ilike 'a%bcd'")
    }

    "properly render IS NULL expression " in {
      DefaultParser
        .parse[IO]("properties.foo is null")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo') is null")
    }

    "properly render IS NOT NULL expression " in {
      DefaultParser
        .parse[IO]("properties.foo is not null")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "json_extract_path_text(json, 'properties','foo') is not null")
    }

    "properly render Intersects bbox  expression" in {
      DefaultParser
        .parse[IO]("intersects bbox")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "ST_Intersects( geometry, 'SRID=31370;POLYGON((1 1,100 1,100 100,1 100,1 1))' )")
    }

    "properly render Intersects with Wkt Literal  expression" in {
      DefaultParser
        .parse[IO]("intersects 'SRID=31370;POINT(10 12)'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        "ST_Intersects( geometry, 'SRID=31370;POINT(10 12)' )")
    }

    "properly render JsonContains expression" in {
      DefaultParser
        .parse[IO](""" properties.test @> '["a", 2]' """)
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """json_extract_path(json, 'properties','test')::jsonb @> '["a", 2]'::jsonb""")
    }

    "properly render to_date functions in expression" in {
      DefaultParser
        .parse[IO](" to_date(properties.foo, 'YYYY-MM-DD') = to_date('2019-04-30', 'YYYY-MM-DD') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """to_date(json_extract_path_text(json, 'properties','foo'), 'YYYY-MM-DD' ) = ( to_date( '2019-04-30' , 'YYYY-MM-DD' ) )""")
    }

    "properly render between .. and operator in expression" in {
      DefaultParser
        .parse[IO](" to_date(properties.foo, 'YYYY-MM-DD') between '2011-01-01' and '2012-01-01' ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """( to_date(json_extract_path_text(json, 'properties','foo'), 'YYYY-MM-DD' ) between '2011-01-01' and '2012-01-01' )""")
    }

  }

  "The PGTableQueryRenderer " should {

    val renderer = PgTableQueryRenderer

    "properly render boolean expressions containing equality expresssions " in {
      DefaultParser
        .parse[IO]("abcd = 12")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""abcd" = ( 12 )""")
    }

    "properly render comparison expressions contain boolean literal" in {
      DefaultParser
        .parse[IO]("abcd = TRUE")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""abcd" = ( true )""")
    }

    "properly render simple equality expression " in {
      DefaultParser
        .parse[IO]("properties.foo = 'bar1'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" = ( 'bar1' )""")
    }

    "properly render boolean expressions containing comparison expresssions other than equality " in {
      (
        DefaultParser
          .parse[IO]("abcd > 12")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(""""abcd" > ( 12 )""")
      ) and (
        DefaultParser
          .parse[IO]("abcd >= 12")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(""""abcd" >= ( 12 )""")
      ) and (
        DefaultParser
          .parse[IO]("abcd != 'abc'")
          .map(expr => compressWS(renderer.render(expr))) must returnValue(
          """"abcd" != ( 'abc' )""")
      )
    }

    "properly render negated expressions " in {
      DefaultParser
        .parse[IO]("not abcd = 12")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """NOT ( "abcd" = ( 12 ) )""")
    }

    "properly render AND expressions " in {
      DefaultParser
        .parse[IO](" (ab = 12) and (cd > 'c') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """( "ab" = ( 12 ) ) AND ( "cd" > ( 'c' ) )""")
    }

    "properly render OR expressions " in {
      DefaultParser
        .parse[IO](" (ab = 12) or (cd > 'c') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """( "ab" = ( 12 ) ) OR ( "cd" > ( 'c' ) )""")
    }

    "properly render a boolean literal as a boolean expression" in {
      DefaultParser.parse[IO]("TRUE").map(expr => renderer.render(expr)) must returnValue(" true ")
    }

    "property render an IN predicate expression" in {
      DefaultParser
        .parse[IO](" ab in (1,2,3) ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""ab" in (3,2,1)""")
    }

    "properly render simple regex expression " in {
      DefaultParser
        .parse[IO]("properties.foo ~ /bar1.*/")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" ~ 'bar1.*'""")
    }

    "properly render simple like expression " in {
      DefaultParser
        .parse[IO]("properties.foo like 'a%bcd'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" like 'a%bcd'""")
    }

    "properly render simple ilike expression " in {
      DefaultParser
        .parse[IO]("properties.foo ilike 'a%bcd'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" ilike 'a%bcd'""")
    }

    "properly render IS NULL expression " in {
      DefaultParser
        .parse[IO]("properties.foo is null")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" is null""")
    }

    "properly render IS NOT NULL expression " in {
      DefaultParser
        .parse[IO]("properties.foo is not null")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(""""foo" is not null""")
    }

    "properly render Intersects bbox  expression" in {
      DefaultParser
        .parse[IO]("intersects bbox")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """ST_Intersects( geometry, 'SRID=31370;POLYGON((1 1,100 1,100 100,1 100,1 1))' )""")
    }

    "properly render Intersects with Wkt Literal  expression" in {
      DefaultParser
        .parse[IO]("intersects 'SRID=31370;POINT(10 12)'")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """ST_Intersects( geometry, 'SRID=31370;POINT(10 12)' )""")
    }

    "properly render JsonContains expression" in {
      DefaultParser
        .parse[IO](""" properties.test @> '["a", 2]' """)
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """"test"::jsonb @> '["a", 2]'::jsonb""")
    }

    "properly render to_date functions in expression" in {
      DefaultParser
        .parse[IO](" to_date( foo, 'YYYY-MM-DD') = to_date('2019-04-30', 'YYYY-MM-DD') ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """to_date("foo", 'YYYY-MM-DD' ) = ( to_date( '2019-04-30' , 'YYYY-MM-DD' ) )""")
    }

    "properly render between .. and operator in expression" in {
      DefaultParser
        .parse[IO](" to_date( foo, 'YYYY-MM-DD') between '2011-01-01' and '2012-01-01' ")
        .map(expr => compressWS(renderer.render(expr))) must returnValue(
        """( to_date("foo", 'YYYY-MM-DD' ) between '2011-01-01' and '2012-01-01' )""")
    }

  }

  private def compressWS(str: String) = str.replaceAll(" +", " ").trim

}
