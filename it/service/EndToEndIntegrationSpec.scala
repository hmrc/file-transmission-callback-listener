package service

import java.time.LocalDate

import org.scalatest.GivenWhenThen
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import play.api.test.Helpers._
import uk.gov.hmrc.play.test.UnitSpec

class EndToEndIntegrationSpec extends UnitSpec with GuiceOneAppPerSuite with GivenWhenThen {

  "End To End Integration" should {
    "post 3 times to ListenerController and poll PollController for response" in {
      Given("the service receives 3 POSTs to /listen")
      for (i <- 1 to 3) {
        val listenRequest = FakeRequest(Helpers.POST, "/upscan-listener/listen", FakeHeaders(), postBodyJson(i))

        val listenResponseF = route(app, listenRequest).get
        status(listenResponseF)        shouldBe 200
        contentAsJson(listenResponseF) shouldBe postBodyJson(i)
      }

      When("the service is polled on /poll")
      val pollRequest = FakeRequest(Helpers.GET, "/upscan-listener/poll")

      val pollResponseF = route(app, pollRequest).get
      status(pollResponseF) shouldBe 200

      Then("the service should return a record of the current date and the 3 POSTs")
      contentAsJson(pollResponseF) shouldBe Json.obj(
        "currentDate" -> LocalDate.now.toString,
        "responses" -> Json.arr(
          postBodyJson(3),
          postBodyJson(2),
          postBodyJson(1)
        )
      )
    }
  }

  private def postBodyJson(i: Int): JsObject =
    Json.obj(
      "reference"   -> s"my-reference-$i",
      "downloadUrl" -> s"http://my$i.download.url"
    )
}
