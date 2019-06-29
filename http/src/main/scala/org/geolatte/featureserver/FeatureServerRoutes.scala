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
    val codec = new Codec[F](dsl)

    import dsl._
    import codec._

    HttpRoutes.of[F] {
      case GET -> Root / "databases" =>
        for {
          databases <- R.listDatabases
          resp      <- Ok(databases.asJson)
        } yield resp
    }
  }
}
