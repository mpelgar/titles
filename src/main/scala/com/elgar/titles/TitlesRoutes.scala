package com.elgar.titles

import cats.effect.Concurrent
import cats.implicits._
import io.circe.generic.auto._
import org.http4s.EntityEncoder.byteArrayEncoder
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}

object TitlesRoutes {

  def titlesRoutes[F[_]: Concurrent](T: Titles[F]): HttpRoutes[F] = {

    case class Url(url: String)
    implicit val directorDecoder: EntityDecoder[F, Url] = jsonOf[F, Url]

    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case GET -> Root / "titles" =>
        for {
          titles <- T.get
          resp <- Ok(titles)
        } yield resp
      case GET -> Root / "titles" / IntVar(id) / "favicon.ico" =>
        for {
          favicon <- T.getFavicon(id)
          resp <- favicon.fold(NotFound())(Ok(_))
        } yield resp
      case req @ POST -> Root / "titles" =>
        for {
          url <- req.as[Url]
          title <- T.insert(url.url)
          resp <- Ok(title)
        } yield resp
    }
  }

}
