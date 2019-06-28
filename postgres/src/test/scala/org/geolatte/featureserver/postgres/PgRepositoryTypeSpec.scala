package org.geolatte.featureserver.postgres

import cats.effect._
import doobie.util.transactor.Transactor
import org.specs2.mutable.Specification


/**
 * Created by Karel Maesen, Geovise BVBA on 2019-06-28.
 */
object PgRepositoryTypeSpec extends Specification with doobie.specs2.IOChecker {

  override val  transactor: doobie.Transactor[IO] =  testTransactor

  check( Sql.listDbs )
}
