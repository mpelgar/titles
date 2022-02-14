package com.elgar.titles

import cats.effect.IO
import com.elgar.titles.ScrapeImpl.TitleAndFaviconUrl
import munit.CatsEffectSuite
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

class ScrapeSpec extends CatsEffectSuite {

  private val browser = JsoupBrowser()

  private val fakeBaseUrl = "http://www.google.com"

  private def scrapeFile(path: String): IO[TitleAndFaviconUrl] = {
    val doc = browser.parseFile(path)
    ScrapeImpl.parseTitleAndFaviconUrl(doc, fakeBaseUrl)
  }

  private def assertTitleAndFavicon(
      filePath: String,
      title: Option[String],
      favicon: Option[String]
  ): IO[Unit] =
    assertIO(
      scrapeFile(filePath),
      TitleAndFaviconUrl(
        title = title,
        faviconUrl = favicon.map(f => s"$fakeBaseUrl$f")
      )
    )

  test("Scraping a valid document should return a title and favicon") {
    assertTitleAndFavicon(
      "src/test/resources/valid.html",
      Some("Some title"),
      Some("/favicon.ico")
    )
  }

  test("Scraping an empty document with return an empty title and favicon") {
    assertTitleAndFavicon("src/test/resources/empty", None, None)
  }

  test(
    "Scraping a document with multiple titles and favicons should return the first"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/twoTitlesAndFavicons.html",
      Some("First title"),
      Some("/favicon1.ico")
    )
  }

  test(
    "Scraping a document should return the favicon if the icon in the rel tag is capitalized"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/faviconCapitalized.html",
      None,
      Some("/favicon.ico")
    )
  }

  test(
    "Scraping a document should return the favicon if the rel tag contains text other than icon"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/faviconShortcutIcon.html",
      None,
      Some("/favicon.ico")
    )
  }

  test(
    "Scraping a document should not find a favicon if the link with rel tag has no href"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/linkWithNoHref.html",
      None,
      None
    )
  }

  test(
    "Scraping a document with an empty title should return an empty string"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/titleWithNoText.html",
      Some(""),
      None
    )
  }

  test(
    "Scraping a document with an empty title should return an empty string"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/titleWithNoText.html",
      Some(""),
      None
    )
  }

  test(
    "Scraping a document that is plain text should return None"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/text",
      None,
      None
    )
  }

  test(
    "Scraping a document that has an unclosed tag should still return values"
  ) {
    assertTitleAndFavicon(
      "src/test/resources/notValid.html",
      Some("Some title"),
      Some("/favicon.ico")
    )
  }

}
