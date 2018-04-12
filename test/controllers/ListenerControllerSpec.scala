package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.test.UnitSpec
import utils.ResponseConsumer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ListenerControllerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout: akka.util.Timeout = 10.seconds

  "ListenerController" should {
    "return OK and write to logs for POST for successful upload" in {
      Given("a request containing correctly formatted JSON and a valid download URL for successful upload")
      val responseConsumer = mock[ResponseConsumer]
      val controller = new ListenerController(responseConsumer)

      val jsonCallback: JsValue = Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://my.download.url"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(jsonCallback)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return OK")
      status(response) shouldBe 200

      And("the raw JSON should be returned in the response")
      Helpers.contentAsJson(response) shouldBe jsonCallback

      And("the successful response should be added to the response consumer log")
      Mockito.verify(responseConsumer).addResponse(any(): JsValue, any(): DateTime)
    }

    "return OK and write to logs for POST for quarantined upload" in {
      Given("a request containing correctly formatted JSON and a valid download URL for quarantined upload")
      val responseConsumer = mock[ResponseConsumer]
      val controller = new ListenerController(responseConsumer)

      val jsonCallback: JsValue = Json.parse(
        """{
          |"reference": "my-reference",
          |"details": "This file upload was infected"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(jsonCallback)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return OK")
      status(response) shouldBe 200

      And("the raw JSON should be returned in the response")
      Helpers.contentAsJson(response) shouldBe jsonCallback


      And("the successful response should be added to the response consumer log")
      Mockito.verify(responseConsumer).addResponse(any(): JsValue, any(): DateTime)
    }

    "return BadRequest for a file that contains invalid body" in {
      Given("a request containing body content that cannot be parsed as JSON")
      val controller = new ListenerController(mock[ResponseConsumer])

      val request = FakeRequest().withTextBody("This is not JSON")

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 400
    }
  }
}
