package com.elgar.titles

import cats.effect.{Async, Resource}
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.Stream
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.staticcontent._
import org.http4s.server.middleware.Logger
import org.http4s.server.staticcontent.FileService

object TitlesServer {

  def stream[F[_]: Async](
      queries: Queries[F],
      scrape: Scrape[F]
  ): Stream[F, Nothing] = {
    for {
      client <- Stream.resource(EmberClientBuilder.default[F].build)

      titlesAlg = Titles.impl[F](client, queries, scrape)

      httpApp = (TitlesRoutes.titlesRoutes[F](titlesAlg) <+>
        fileService(
          // This could be improved.  Having the target path here with the scala version is a little strange
          FileService.Config("./target/scala-2.13/classes/assets")
        )).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- Stream.resource(
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080") // This could come from config
          .withHttpApp(finalHttpApp)
          .build >>
          Resource.eval(Async[F].never)
      )
    } yield exitCode
  }.drain
}
