package nl.kevinvandervlist.ohti.portal

import com.typesafe.scalalogging.LazyLogging
import io.circe
import sttp.client3.{Identity, RequestT, Response, ResponseException, SttpBackend}

trait Client[T] extends LazyLogging {
  protected implicit val backend: SttpBackend[Identity, Any]

  def doRequest(logMsg: String,
                //resp: RequestT[Identity, T, Any]
                resp: RequestT[Identity, Either[ResponseException[String, io.circe.Error], T], Any]
             ): Option[T] = {
    handle(logMsg, resp.send(backend))
  }

  def handle(msg: String, response: Identity[Response[Either[ResponseException[String, circe.Error], T]]]): Option[T] = {
    if(! response.isSuccess) {
      logger.error("Failure: {}: {}", response.code, response.body)
      None
    } else {
      val body: Either[ResponseException[String, circe.Error], T] = response.body
      if(body.isLeft) {
        logger.error(msg, response.code, body.left)
        None
      } else {
        body.toOption
      }
    }
  }
}
