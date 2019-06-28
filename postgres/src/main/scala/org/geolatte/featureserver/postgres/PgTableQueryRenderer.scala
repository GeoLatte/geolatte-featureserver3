package org.geolatte.featureserver.postgres

import org.geolatte.featureserver.QueryExpr._

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */
object PgTableQueryRenderer extends QueryRenderer {

  override def renderPropertyExpr(expr: PropertyExpr): String = {
    val withoutPrefix =
      if (expr.path.trim.startsWith("properties.")) {
        expr.path.trim.substring(11)
      } else {
        expr.path
      }
    s""""$withoutPrefix""""
  }

  override def cast(exp: Expr): String = "" // don't cast

}

