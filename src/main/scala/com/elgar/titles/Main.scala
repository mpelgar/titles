package com.elgar.titles

import cats.effect.{ExitCode, IO, IOApp}
import doobie.Transactor

object Main extends IOApp {

  private val xa = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = "jdbc:postgresql://db:5432/postgres", // This should come from config
    user = "postgres",
    pass = "password"
  )

  def run(args: List[String]): IO[ExitCode] =
    TitlesServer
      .stream[IO](new QueriesImpl(xa), ScrapeImpl)
      .compile
      .drain
      .as(ExitCode.Success)
}
