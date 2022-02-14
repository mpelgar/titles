package com.elgar.titles

import cats.effect.IO
import cats.implicits._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document
import net.ruippeixotog.scalascraper.scraper.ContentExtractors.elementList
import org.http4s.client.Client
import org.log4s.getLogger

trait Scrape[F[_]] {
  def getTitleAndFavicon(url: String, client: Client[F]): F[TitleAndFavicon]
}

object ScrapeImpl extends Scrape[IO] {

  private[this] val logger = getLogger

  override def getTitleAndFavicon(
      url: String,
      client: Client[IO]
  ): IO[TitleAndFavicon] = {
    (for {
      doc <- getDocument(url)
      titleAndFaviconUrl <- parseTitleAndFaviconUrl(doc, url)
      faviconFile <-
        titleAndFaviconUrl.faviconUrl
          .traverse(faviconUrl =>
            client.get(faviconUrl)(r => r.body.compile.to(Array))
          )
    } yield TitleAndFavicon(
      title = titleAndFaviconUrl.title,
      favicon = faviconFile
    )).attempt.map { a =>
      a.leftMap(t =>
        logger.error(t)("getting title and favicon failed") // Impure
      )
      // If an exception is thrown it will insert a row with no title or favicon.
      // Another option would be to return an error to the user.
      a.getOrElse(TitleAndFavicon(title = None, favicon = None))
    }
  }

  private def getDocument(url: String): IO[Document] = {
    val browser = JsoupBrowser()
    IO.blocking(browser.get(url))
  }

  case class TitleAndFaviconUrl(
      title: Option[String],
      faviconUrl: Option[String]
  )

  /** Get the title from the first title tag. Get the favicon by looking for the
    * first link tag with a rel attribute that contains the word icon anywhere
    * in it.
    */
  def parseTitleAndFaviconUrl(
      doc: Document,
      url: String
  ): IO[TitleAndFaviconUrl] = IO.blocking {
    val title = doc >?> text("title")

    // "link[rel*='icon' i] didn't work so manually ignoring case and checking if it contains icon
    val faviconUrl =
      (doc >> elementList("link[rel]"))
        .find(_.attr("rel").toLowerCase.contains("icon"))
        .flatMap(_ >?> attr("href"))

    // prepend the url of the page if it doesn't start with http/https
    val fullFaviconUrl = faviconUrl.map(f =>
      if (f.startsWith("http://") || f.startsWith("https://"))
        f
      else s"$url$f"
    )

    TitleAndFaviconUrl(title = title, faviconUrl = fullFaviconUrl)
  }

}
