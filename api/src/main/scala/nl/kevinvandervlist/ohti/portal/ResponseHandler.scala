package nl.kevinvandervlist.ohti.portal

import com.typesafe.scalalogging.LazyLogging
import io.circe
import sttp.client.{Identity, Response, ResponseError}

trait ResponseHandler[T] extends LazyLogging{
  def handle(msg: String, response: Identity[Response[Either[ResponseError[circe.Error], T]]]): Option[T] = {
    if(response.code.isSuccess) {
      response.body.toOption
    } else {
      logger.error(msg, response.code, response.body.toString)
      None
    }
  }
}
