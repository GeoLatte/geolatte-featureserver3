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

package org.geolatte.featureserver.postgres

import doobie.util.log.LogHandler.getClass
import doobie.util.log.{ExecFailure, LogHandler, ProcessingFailure, Success}
import org.slf4j.{Logger, LoggerFactory}

/**
 * Created by Karel Maesen, Geovise BVBA on 2019-07-03.
 */
object StatementLogger {

  val logger = LoggerFactory.getLogger( "StatementLogger" )

  val handler = LogHandler {

               case Success(s, a, e1, e2) =>
                 logger.info(s"""Successful Statement Execution:
                                   |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                                   |
            | arguments = [${a.mkString(", ")}]
                                   |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (${(e1 + e2).toMillis} ms total)
          """.stripMargin )

               case ProcessingFailure(s, a, e1, e2, t) =>
                 logger.error(s"""Failed Resultset Processing:
                                     |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                                     |
            | arguments = [${a.mkString(", ")}]
                                     |   elapsed = ${e1.toMillis} ms exec + ${e2.toMillis} ms processing (failed) (${(e1 + e2).toMillis} ms total)
                                     |   failure = ${t.getMessage}
          """.stripMargin )

               case ExecFailure(s, a, e1, t) =>
                 logger.error(s"""Failed Statement Execution:
                                     |
            |  ${s.linesIterator.dropWhile(_.trim.isEmpty).mkString("\n  ")}
                                     |
            | arguments = [${a.mkString(", ")}]
                                     |   elapsed = ${e1.toMillis} ms exec (failed)
                                     |   failure = ${t.getMessage}
          """.stripMargin )

             }
}

