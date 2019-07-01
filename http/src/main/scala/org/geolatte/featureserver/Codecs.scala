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

package org.geolatte.featureserver

import io.circe.{Encoder, Json}
import org.geolatte.featureserver.Domain.{Schema, Table}

/**
  * Created by Karel Maesen, Geovise BVBA on 2019-07-01.
  */
object Codecs {

  sealed trait CodecVersion

  object V1 extends CodecVersion {
    implicit val encodeDb: Encoder[Schema] = new Encoder[Schema] {
      final def apply(a: Schema): Json = Json.obj(
        ("name", Json.fromString(a.name)),
        ("url", Json.fromString(s"api/databases/${a.name}"))
      )
    }

    implicit val encodeCollection: Encoder[Table] = new Encoder[Table] {
      final def apply(a: Table): Json = Json.obj(
        "name" -> Json.fromString(a.name),
        "url"  -> Json.fromString(s"/api/databases/${a.schema.name}/${a.name}")
      )
    }
  }

  object V2 extends CodecVersion {
    implicit val encodeDb: Encoder[Schema] = new Encoder[Schema] {
      final def apply(a: Schema): Json = Json.obj(
        ("name", Json.fromString(a.name))
      )
    }

    implicit val encodeCollection: Encoder[Table] = new Encoder[Table] {
      final def apply(a: Table): Json = Json.obj(
        "name" -> Json.fromString(a.name)
      )
    }

  }

}
