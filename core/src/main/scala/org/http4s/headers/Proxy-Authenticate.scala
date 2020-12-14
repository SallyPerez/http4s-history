/*
 * Copyright 2013 http4s.org
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

package org.http4s
package headers

import cats.syntax.all._
import cats.data.NonEmptyList
import cats.parse.Parser1
import org.http4s.internal.parsing.Rfc7235

object `Proxy-Authenticate`
    extends HeaderKey.Internal[`Proxy-Authenticate`]
    with HeaderKey.Recurring {
  private[http4s] val parser: Parser1[`Proxy-Authenticate`] =
    Rfc7235.challenges.map(`Proxy-Authenticate`.apply)

  override def parse(s: String): ParseResult[`Proxy-Authenticate`] =
    parser.parseAll(s).leftMap(err => ParseFailure("Invalid Proxy-Authenticate", err.toString))
}

/** {{{
  *   The "Proxy-Authenticate" header field consists of at least one
  *   challenge that indicates the authentication scheme(s) and parameters
  *   applicable to the proxy for this effective request URI...
  * }}}
  * From [[https://tools.ietf.org/html/rfc7235#section-4.3 RFC-7235]]
  */
final case class `Proxy-Authenticate`(values: NonEmptyList[Challenge])
    extends Header.RecurringRenderable {
  override def key: `Proxy-Authenticate`.type = `Proxy-Authenticate`
  type Value = Challenge
}
