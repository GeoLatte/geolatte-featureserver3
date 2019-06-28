package org.geolatte.featureserver

import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Json}
import org.geolatte.featureserver.Domain._
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import io.circe.syntax._
import org.http4s.circe._

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
  */
object FeatureServerRoutes {


  def metaRoutes[F[_]: Sync](R: Repository[F]): HttpRoutes[F] = {

    val dsl = new Http4sDsl[F] {}
    import dsl._

    object Uris {
      val databases = Root / "api" / "databases"
    }

    implicit val encodeDb: Encoder[Database] = new Encoder[Database] {
      final def apply(a: Database): Json = Json.obj(
        ("name", Json.fromString(a.name)),
        ("url", Json.fromString( (Uris.databases  / a.name).toString()))
        )
    }

    HttpRoutes.of[F] {
      case GET -> Uris.databases =>
        for {
          databases <- R.listDatabases
          resp <- Ok(databases.asJson)
        } yield resp
    }
  }
}
