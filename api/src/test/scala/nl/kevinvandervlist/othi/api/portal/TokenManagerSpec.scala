package nl.kevinvandervlist.othi.api.portal

import nl.kevinvandervlist.othi.portal.TokenManager
import org.scalatest.wordspec.AnyWordSpec
import sttp.client.Response
import sttp.client.testing.SttpBackendStub
import sttp.model.{Method, StatusCode}
import nl.kevinvandervlist.othi.portal.TokenManager._
import org.scalatest.matchers.should.Matchers

class TokenManagerSpec extends AnyWordSpec with Matchers {
  private val initial = TokenResponse("access_1", "type", 100, "refresh_1")
  private val refreshed = TokenResponse("access_2", "type", 100, "refresh_2")

  implicit val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatchesPartial {
        case r if r.method == Method.POST && r.uri.path.startsWith(List("api", "tokens")) =>
          if(r.body.toString.contains("username=61")) {
            Response.ok(initial)
          } else if(r.body.toString.contains(s"refresh_token=${initial.refresh_token}")) {
            Response.ok(refreshed)
          } else {
            Response.apply("", StatusCode.BadRequest)
          }
      }

  "The Token manager" should {
    val tokenManager = new TokenManager()

    "be able to login with valid user data" in {
      val tokenInfo = tokenManager.requestToken("a", "abc")
      tokenInfo.isDefined shouldBe true
      tokenInfo.get.access_token shouldBe initial.access_token
    }
    "be able to refresh an existing token" in {
      val tokenInfo = tokenManager.refreshToken(initial)
      tokenInfo.isDefined shouldBe true
      tokenInfo.get.access_token shouldBe refreshed.access_token
    }
    "not be able to login with invalid user data" in {
      tokenManager.requestToken("c", "abc") shouldBe None
    }
  }
}
