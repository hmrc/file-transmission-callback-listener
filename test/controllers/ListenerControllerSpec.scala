package controllers

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import connectors.FileHashRetriever
import model.{ListenerRequest, ListenerResponse}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.test.UnitSpec
import utils.{CallbackConsumer, ResponseConsumer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class ListenerControllerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout: akka.util.Timeout = 10.seconds

  private val connector = new FileHashRetrieverImpl()

  "ListenerController" should {
    "return OK and write to logs for correctly formatted POST" in {
      Given("a request containing correctly formatted JSON and a valid download URL")
      val callbackConsumer = mock[CallbackConsumer]
      val responseConsumer = mock[ResponseConsumer]
      val controller = new ListenerController(connector, callbackConsumer, responseConsumer)

      val validJson: JsValue = Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://my.download.url"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(validJson)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return OK")
      status(response) shouldBe 200

      And("the download URL, the file reference and the checksum should be return as JSON")
      Helpers.contentAsJson(response) shouldBe Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://my.download.url",
          |"hash": "e7e5955a9926ff43412fcb4ff4e65e68"
        }""".stripMargin)

      And("the download URL, the file reference and the checksum should be sent to the callbackConsumer")
      val awaited = Await.result(response, 2.seconds)
      val listenerResponse = ListenerResponse("my-reference", new URL("http://my.download.url"), "e7e5955a9926ff43412fcb4ff4e65e68")
      Mockito.verify(callbackConsumer).logSuccessfulResponse(listenerResponse)

      And("the successful response should be added to the response consumer log")
      Mockito.verify(responseConsumer).addResponse(any(), any())
    }

    "return InternalServiceError and write to logs for a file that cannot be hashed correctly" in {
      Given("a request containing correctly formatted JSON and a download URL that fails hashing")
      val callbackConsumer = mock[CallbackConsumer]
      val controller = new ListenerController(connector, callbackConsumer, mock[ResponseConsumer])

      val invalidJson: JsValue = Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://some.invalid.url"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(invalidJson)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 500

      And("the download URL, the file reference and the error should be sent to the callbackConsumer")
      val awaited = Await.result(response, 2.seconds)
      val listenerRequest = ListenerRequest("my-reference", new URL("http://some.invalid.url"))
      Mockito.verify(callbackConsumer).logHashError(listenerRequest, "This is an expected hashing exception")
    }

    "return BadRequest and write to logs for a file that contains invalid JSON" in {
      Given("a request containing incorrectly formatted JSON")
      val callbackConsumer = mock[CallbackConsumer]
      val controller = new ListenerController(connector, callbackConsumer, mock[ResponseConsumer])

      val invalidJson: JsValue = Json.parse(
        """{
          |"some-key": "my-reference",
          |"some-reference": "http://some.invalid.url"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(invalidJson)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 400

      And("the JSON should be sent to the consumer")
      val awaited = Await.result(response, 2.seconds)
      Mockito.verify(callbackConsumer).logInvalidJson(invalidJson)
    }

    "return BadRequest and write to logs for a file that contains invalid body" in {
      Given("a request containing body content that cannot be parsed as JSON")
      val callbackConsumer = mock[CallbackConsumer]
      val controller = new ListenerController(connector, callbackConsumer, mock[ResponseConsumer])

      val request = FakeRequest().withTextBody("This is not JSON")

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 400

      And("the request body should be sent to the callbackConsumer")
      val awaited = Await.result(response, 2.seconds)
      Mockito.verify(callbackConsumer).logInvalidBody("This is not JSON")
    }
  }

  class FileHashRetrieverImpl extends FileHashRetriever {
    override def fileHash(url: URL): Future[String] = {
      url.toString match {
        case "http://some.invalid.url" => Future.failed(new Exception("This is an expected hashing exception"))
        case _ => Future.successful("e7e5955a9926ff43412fcb4ff4e65e68")
      }
    }
  }

}
