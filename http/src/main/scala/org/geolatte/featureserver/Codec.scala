package org.geolatte.featureserver

import io.circe.{Encoder, Json}
import org.geolatte.featureserver.Domain.Database
import org.http4s.dsl.Http4sDsl

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-06-29.
  */
class Codec[F[_]](dsl: Http4sDsl[F]) {

  import dsl._

  implicit val encodeDb: Encoder[Database] = new Encoder[Database] {
    final def apply(a: Database): Json = Json.obj(
      ("name", Json.fromString(a.name)),
      ("url", Json.fromString((Root / "databases" / a.name).toString()))
    )
  }

}
