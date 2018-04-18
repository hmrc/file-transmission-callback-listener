package controllers

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import model.ResponseLog
import java.time.LocalDate
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.play.test.UnitSpec
import utils.ResponseConsumer

import scala.concurrent.Future
import scala.concurrent.duration._

class PollControllerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {

  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val timeout: akka.util.Timeout = 10.seconds

  "PollController" should {
    "return successful responses entries from local log" in {

      Given("a controller and entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ???

        override def retrieveResponses: ResponseLog = ResponseLog(
          currentDate = LocalDate.parse("2018-03-16"),
          responses = List(
            Json.obj("reference" -> "my-first-reference", "url" -> "http://url.one", "fileStatus" -> "READY"),
            Json.obj("reference" -> "my-second-reference", "details" -> "This file had a virus", "fileStatus" -> "FAILED"),
            Json.obj("reference" -> "my-third-reference", "url" -> "http://url.three", "fileStatus" -> "READY")
          )
        )
      }

      val controller = new PollController(responseConsumer)

      When("the poll method is called")
      val result: Future[Result] = controller.poll()(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("the list of events and date in the event store should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse(
        """
          |{
          |	"currentDate": "2018-03-16",
          |	"responses": [{
          |		"reference": "my-first-reference",
          |		"url": "http://url.one",
          |		"fileStatus": "READY"
          |	}, {
          |		"reference": "my-second-reference",
          |		"details": "This file had a virus",
          |		"fileStatus": "FAILED"
          |	}, {
          |		"reference": "my-third-reference",
          |		"url": "http://url.three",
          |		"fileStatus": "READY"
          |	}]
          |}
        """.stripMargin)
    }

    "return successful from empty local log" in {

      Given("a controller and NO entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ()

        override def retrieveResponses: ResponseLog = ResponseLog(LocalDate.parse("2018-03-16"), Nil)
      }

      val controller = new PollController(responseConsumer)

      When("the poll method is called")
      val result: Future[Result] = controller.poll()(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("an empty list and date in the event store should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse(
        """
          |{
          |	"currentDate": "2018-03-16",
          |	"responses":[]
          |}
        """.stripMargin)
    }
  }
}
