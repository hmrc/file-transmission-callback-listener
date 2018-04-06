package controllers

import java.net.URL

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import model.{ListenerResponseSuccessfulUpload, QuarantinedFile, ResponseLog}
import org.joda.time.DateTime
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.Json
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
        override def addResponse(response: ListenerResponseSuccessfulUpload, currentDate: DateTime): Unit = ()
        override def addResponse(response: QuarantinedFile, currentDate: DateTime): Unit = ()
        override def retrieveResponses: ResponseLog = ResponseLog(
          currentDate = DateTime.parse("2018-03-16"),
          successfulResponses = List(
            ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
            ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
          ),
          quarantineResponses = List(
            QuarantinedFile("my-third-reference", "This file had a nasty virus")
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
          |	"successfulResponses": [{
          |		"reference": "my-first-reference",
          |		"downloadUrl": "http://url.one",
          |		"hash": "11111"
          |	}, {
          |		"reference": "my-second-reference",
          |		"downloadUrl": "http://url.two",
          |		"hash": "22222"
          |	}],
          |	"quarantineResponses": [{
          |		"reference": "my-third-reference",
          |		"details": "This file had a nasty virus"
          |	}]
          |}
        """.stripMargin)
    }

    "return successful from empty local log" in {

      Given("a controller and NO entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: ListenerResponseSuccessfulUpload, currentDate: DateTime): Unit = ()
        override def addResponse(response: QuarantinedFile, currentDate: DateTime): Unit = ()
        override def retrieveResponses: ResponseLog = ResponseLog(DateTime.parse("2018-03-16"), Nil, Nil)
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
          |	"successfulResponses":[],
          | "quarantineResponses":[]
          |}
        """.stripMargin)
    }
  }
}
