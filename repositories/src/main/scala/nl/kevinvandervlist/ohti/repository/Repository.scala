package nl.kevinvandervlist.ohti.repository

import scala.util.Try

trait Repository[T, K] {
  def exists(id: K): Try[Boolean] = get(id).map(_.isDefined)
  def get(id: K): Try[Option[T]]
  def put(item: T): Try[T]
  def dispose(): Unit
}
