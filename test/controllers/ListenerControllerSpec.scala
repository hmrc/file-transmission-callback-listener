package controllers

import java.io.File
import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import connectors.FileHashRetriever
import org.mockito.Mockito
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.test.UnitSpec
import utils.LogHelper
import org.mockito.ArgumentMatchers.any

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._

/**
  * Created by joanna.pinto on 12/03/2018.
  */
class ListenerControllerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {


  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout: akka.util.Timeout = 10.seconds

  private val connector = new FileHashRetrieverImpl()

  "ListenerController" should {
    "return OK and write to logs for correctly formatted POST" in {
      Given("a request containing correctly formatted JSON and a valid download URL")
      val logger = mock[LogHelper]
      val controller = new ListenerController(connector, logger)

      val validJson: JsValue = Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://my.download.url"
        }""".stripMargin)

      val request = FakeRequest().withJsonBody(validJson)

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)
      val awaited = Await.result(response, 2.seconds)

      Then("the download URL, the file reference and the checksum should be written to the logs")
      Mockito.verify(logger).logSuccessfulResponse(any())

      And("the service should return OK")
      status(response) shouldBe 200

      And("the download URL, the file reference and the checksum should be return as JSON")
      Helpers.contentAsJson(response) shouldBe Json.parse(
        """{
          |"reference": "my-reference",
          |"downloadUrl": "http://my.download.url",
          |"hash": "e7e5955a9926ff43412fcb4ff4e65e68"
        }""".stripMargin)
    }

    "return InternalServiceError and write to logs for a file that cannot be hashed correctly" in {
      Given("a request containing correctly formatted JSON and a download URL that fails hashing")
      val logger = mock[LogHelper]
      val controller = new ListenerController(connector, logger)

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

      And("the download URL, the file reference and the error should be written to the logs")
      val awaited = Await.result(response, 2.seconds)
      Mockito.verify(logger).logHashError(any(), any())
    }

    "return BadRequest and write to logs for a file that contains invalid JSON" in {
      Given("a request containing incorrectly formatted JSON")
      val logger = mock[LogHelper]
      val controller = new ListenerController(connector, logger)

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

      And("the JSON should be written to the logs")
      val awaited = Await.result(response, 2.seconds)
      Mockito.verify(logger).logInvalidJson(any())
    }

    "return BadRequest and write to logs for a file that contains invalid body" in {
      Given("a request containing body content that cannot be parsed as JSON")
      val logger = mock[LogHelper]
      val controller = new ListenerController(connector, logger)

      val request = FakeRequest().withTextBody("This is not JSON")

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 400

      And("the request body should be written to the logs")
      val awaited = Await.result(response, 2.seconds)
      Mockito.verify(logger).logInvalidBody("This is not JSON")
    }
  }

  class FileHashRetrieverImpl extends FileHashRetriever {
    override def fileHash(url: URL): Future[String] = {
      url.toString match {
        case "http://some.invalid.url" => Future.failed(new Exception("This is an expected hashing exception"))
        case _ =>       Future.successful("e7e5955a9926ff43412fcb4ff4e65e68")
      }
    }
  }
}
