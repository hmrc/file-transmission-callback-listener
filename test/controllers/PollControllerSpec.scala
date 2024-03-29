/*
 * Copyright 2022 HM Revenue & Customs
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

import java.time.LocalDate

import org.apache.pekko.actor.ActorSystem
import model.ResponseLog
import org.mockito.MockitoSugar
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import utils.ResponseConsumer

import scala.concurrent.Future

class PollControllerSpec extends AnyWordSpecLike with Matchers with GivenWhenThen with MockitoSugar {

  implicit val actorSystem                = ActorSystem()
//  implicit val timeout: org.apache.pekko.util.Timeout = 10.seconds

  "PollController" should {

    "return successful responses entries from local log" in {

      Given("a controller and entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ???

        override def retrieveResponses: ResponseLog = ResponseLog(
          currentDate = LocalDate.parse("2018-03-16"),
          responses = List(
            Json.obj("fileReference" -> "my-first-reference", "batchId" -> "B1", "outcome" -> "READY"),
            Json.obj(
              "fileReference"        -> "my-second-reference",
              "batchId"              -> "B2",
              "errorDetails"         -> "Something went wrong",
              "outcome"              -> "FAILED"),
            Json.obj("fileReference" -> "my-third-reference", "batchId" -> "B3", "outcome" -> "READY")
          )
        )

        override def lookupResponseForReference(reference: String): Option[JsValue] = ???
        override def clear() = ???
      }

      val controller = new PollController(responseConsumer, stubMessagesControllerComponents())

      When("the poll method is called")
      val result: Future[Result] = controller.poll()(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("the list of events and date in the event store should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse("""
          |{
          |	"currentDate": "2018-03-16",
          |	"responses": [{
          |		"fileReference": "my-first-reference",
          |		"batchId": "B1",
          |		"outcome": "READY"
          |	}, {
          |		"fileReference": "my-second-reference",
          |   "batchId": "B2",
          |		"errorDetails": "Something went wrong",
          |		"outcome": "FAILED"
          |	}, {
          |		"fileReference": "my-third-reference",
          |   "batchId": "B3",
          |		"outcome": "READY"
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
        override def clear() = ???
      }

      val controller = new PollController(responseConsumer, stubMessagesControllerComponents())

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
          Some(Json.obj("fileReference" -> reference, "batchId" -> "B1", "outcome" -> "READY"))
        override def clear() = ???
      }

      val controller = new PollController(responseConsumer, stubMessagesControllerComponents())

      When("the lookup method is called")
      val result: Future[Result] = controller.lookup("my-first-reference")(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 200

      And("the event should be returned as JSON")
      Helpers.contentAsJson(result) shouldBe Json.parse("""
                                                          |{
                                                          |		"fileReference": "my-first-reference",
                                                          |		"batchId": "B1",
                                                          |		"outcome": "READY"
                                                          |}
                                                        """.stripMargin)

    }

    "return not found if response not found in local log" in {

      Given("a controller and entries in the local log")
      val responseConsumer = new ResponseConsumer {
        override def addResponse(response: JsValue, currentDate: LocalDate): Unit = ???

        override def retrieveResponses: ResponseLog                                 = ???
        override def lookupResponseForReference(reference: String): Option[JsValue] = None
        override def clear() = ???
      }

      val controller = new PollController(responseConsumer, stubMessagesControllerComponents())

      When("the lookup method is called")
      val result: Future[Result] = controller.lookup("my-first-reference")(FakeRequest())

      Then("the service should return OK")
      status(result) shouldBe 404

    }
  }
}
