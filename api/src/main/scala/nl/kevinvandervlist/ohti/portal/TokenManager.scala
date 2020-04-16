package nl.kevinvandervlist.ohti.portal

import com.typesafe.scalalogging.LazyLogging
import io.circe
import io.circe.{Decoder, Encoder, HCursor, Json}
import nl.kevinvandervlist.ohti.portal.TokenManager._
import sttp.client._
import sttp.client.circe._

object TokenManager {
  case class TokenResponse(access_token: String, token_type: String, expires_in: Int, refresh_token: String)
  type TokenProvider = () => Option[TokenResponse]

  implicit val decodeTokenResponse: Decoder[TokenResponse] = new Decoder[TokenResponse] {
    final def apply(c: HCursor): Decoder.Result[TokenResponse] =
      for {
        at <- c.downField("access_token").as[String]
        tt <- c.downField("token_type").as[String]
        exp <- c.downField("expires_in").as[Int]
        rt <- c.downField("refresh_token").as[String]
      } yield {
        TokenResponse(at, tt, exp, rt)
      }
  }

  implicit val encodeTokenResponse: Encoder[TokenResponse] = new Encoder[TokenResponse] {
    final def apply(r: TokenResponse): Json = Json.obj(
      ("access_token", Json.fromString(r.access_token)),
      ("token_type", Json.fromString(r.token_type)),
      ("expires_in", Json.fromInt(r.expires_in)),
      ("refresh_token", Json.fromString(r.access_token))
    )
  }
}

class TokenManager(private implicit val endpoint: Endpoint,
                   private implicit val backend: SttpBackend[Identity, Nothing, NothingT]) extends LazyLogging {

  def refreshToken(previous: TokenResponse): Option[TokenResponse] = {
    val request = Util.baseRequest
      .body(Map(
        "refresh_token" -> previous.refresh_token,
        "grant_type" -> "refresh_token"
      ))
      .post(endpoint.tokens)

    handleResponse("refreshToken", request
      .response(asJson[TokenResponse])
      .send()
    )
  }

  def requestToken(user: String, pass: String): Option[TokenResponse] = {
    val request = Util.baseRequest
      .body(Map(
        "username" -> encodedUsername(user),
        "password" -> pass,
        "grant_type" -> "password"
      ))
      .post(endpoint.tokens)

    handleResponse("requestToken", request
      .response(asJson[TokenResponse])
      .send()
    )
  }

  private def handleResponse(method: String, response: Identity[Response[Either[ResponseError[circe.Error], TokenResponse]]]): Option[TokenResponse] = {
    if(response.code.isSuccess) {
      // TODO: this behaves differently under test & runtime...
      if(response.body.isInstanceOf[TokenResponse]) {
        // This is required for test
        Some(response.body.asInstanceOf[TokenResponse])
      } else {
        // but this happens in production
        response.body.toOption
      }
    } else {
      logger.error("{} failed; got status {} - {}", method, response.code, response.body.toString)
      None
    }
  }

  /** Itho uses a hexademical encoding for the password when sending it to the login endpoint */
  private def encodedUsername(username: String): String = username
    .toList
    .map(_.toInt.toHexString)
    .mkString
}
