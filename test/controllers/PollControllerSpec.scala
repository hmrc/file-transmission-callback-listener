/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

  implicit val actorSystem                = ActorSystem()
  implicit val materializer               = ActorMaterializer()
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
            Json.obj(
              "reference"        -> "my-second-reference",
              "details"          -> "This file had a virus",
              "fileStatus"       -> "FAILED"),
            Json.obj("reference" -> "my-third-reference", "url" -> "http://url.three", "fileStatus" -> "READY")
          )
        )

        override def lookupResponseForReference(reference: String): Option[JsValue] = ???
      }

      val controller = new PollController(responseConsumer)

      When("the poll method is called")
      val result: Future[Result] = controller.poll()(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("the list of events and date in the event store should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse("""
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

        override def lookupResponseForReference(reference: String): Option[JsValue] = ???
      }

      val controller = new PollController(responseConsumer)

      When("the poll method is called")
      val result: Future[Result] = controller.poll()(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("an empty list and date in the event store should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse("""
          |{
          |	"currentDate": "2018-03-16",
          |	"responses":[]
          |}
        """.stripMargin)
    }

    "lookup for existing response in local log" in {

      Given("a controller and entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ???

        override def retrieveResponses: ResponseLog = ???
        override def lookupResponseForReference(reference: String): Option[JsValue] =
          Some(Json.obj("reference" -> reference, "url" -> "http://url.one", "fileStatus" -> "READY"))
      }

      val controller = new PollController(responseConsumer)

      When("the lookup method is called")
      val result: Future[Result] = controller.lookup("my-first-reference")(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("the event should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse("""
                                                          |{
                                                          |		"reference": "my-first-reference",
                                                          |		"url": "http://url.one",
                                                          |		"fileStatus": "READY"
                                                          |}
                                                        """.stripMargin)

    }

    "return not found if response not found in local log" in {

      Given("a controller and entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ???

        override def retrieveResponses: ResponseLog                                 = ???
        override def lookupResponseForReference(reference: String): Option[JsValue] = None
      }

      val controller = new PollController(responseConsumer)

      When("the lookup method is called")
      val result: Future[Result] = controller.lookup("my-first-reference")(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 404

    }
  }
}
