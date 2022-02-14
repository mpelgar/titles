package com.elgar.titles

import cats.syntax.all._
import cats.FlatMap
import org.http4s.client.Client

trait Titles[F[_]] {
  def get: F[List[Title]]
  def getFavicon(id: Int): F[Option[Array[Byte]]]
  def insert(url: String): F[Title]
}

object Titles {

  def impl[F[_]: FlatMap](
      client: Client[F],
      queries: Queries[F],
      scrape: Scrape[F]
  ): Titles[F] = new Titles[F] {

    override def get: F[List[Title]] = {
      queries.list
    }
    override def getFavicon(id: Int): F[Option[Array[Byte]]] = {
      queries.getFavicon(id)
    }

    override def insert(url: String): F[Title] = {
      val urlWithProtocol = ensureProtocol(url)

      for {
        titleAndFavicon <- scrape.getTitleAndFavicon(urlWithProtocol, client)
        title <- queries.insert(
          TitleFaviconAndUrl(
            url = urlWithProtocol,
            favicon = titleAndFavicon.favicon,
            title = titleAndFavicon.title
          )
        )
      } yield title
    }

  }

  def ensureProtocol(url: String): String =
    if (url.startsWith("http://") || url.startsWith("https://"))
      url
    else if (url.startsWith("www."))
      s"https://$url"
    else
      s"https://www.$url"
}
