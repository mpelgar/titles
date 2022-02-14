package com.elgar.titles

import cats.effect.IO
import doobie.implicits._
import doobie.util.transactor.Transactor

trait Queries[F[_]] {
  def list: F[List[Title]]
  def getFavicon(id: Int): F[Option[Array[Byte]]]
  def insert(title: TitleFaviconAndUrl): F[Title]
}

class QueriesImpl(xa: Transactor[IO]) extends Queries[IO] {
  override def list: IO[List[Title]] =
    sql"select id, title, url, favicon is not null from title order by id desc"
      .query[Title]
      .to[List]
      .transact(xa)

  override def getFavicon(id: Int): IO[Option[Array[Byte]]] =
    sql"select favicon from title where id = $id"
      .query[Option[Array[Byte]]]
      .option
      .map(_.flatten)
      .transact(xa)

  def insert(title: TitleFaviconAndUrl): IO[Title] = {
    (for {
      id <-
        sql"insert into title (title, url, favicon) values (${title.title}, ${title.url}, ${title.favicon})".update
          .withUniqueGeneratedKeys[Int]("id")
      title <-
        sql"select id, title, url, favicon is not null from title where id = $id"
          .query[Title]
          .unique
    } yield title).transact(xa)
  }

}
