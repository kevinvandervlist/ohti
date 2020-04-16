package nl.kevinvandervlist.ohti.portal

import com.typesafe.scalalogging.LazyLogging
import io.circe
import sttp.client.{Identity, NothingT, Request, RequestT, Response, ResponseError, SttpBackend}

trait Client[T] extends LazyLogging {
  protected implicit val backend: SttpBackend[Identity, Nothing, NothingT]

  def doRequest(logMsg: String,
              req: Request[Either[String, String], Nothing],
              resp: RequestT[Identity, Either[ResponseError[circe.Error], T], Nothing]
             ): Option[T] = {
    handle(logMsg, resp.send())
  }

  def handle(msg: String, response: Identity[Response[Either[ResponseError[circe.Error], T]]]): Option[T] = {
    if(response.code.isSuccess) {
      response.body.toOption
    } else {
      logger.error(msg, response.code, response.body.toString)
      None
    }
  }
}
