package com.elgar.titles

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder
import org.http4s.EntityEncoder
import org.http4s.circe.jsonEncoderOf

case class Title(
    id: Int,
    title: Option[String],
    url: String,
    hasFavicon: Boolean
)

object Title {
  implicit val titleEncoder: Encoder[Title] = deriveEncoder[Title]
  implicit def titleEntityEncoder[F[_]]: EntityEncoder[F, Title] =
    jsonEncoderOf
}
