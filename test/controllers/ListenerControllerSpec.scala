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
import org.mockito.ArgumentMatchers.any
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.Helpers.{stubMessagesControllerComponents, _}
import play.api.test.{FakeRequest, Helpers}
import utils.ResponseConsumer

import scala.concurrent.Future

class ListenerControllerSpec extends AnyWordSpecLike with Matchers with GivenWhenThen with MockitoSugar {

  implicit val actorSystem = ActorSystem()

  "ListenerController" should {
    "return OK and write to logs for POST for successful upload" in {
      Given("a request containing correctly formatted JSON and a valid download URL for successful upload")
      val responseConsumer = mock[ResponseConsumer]
      val controller = new ListenerController(responseConsumer, stubMessagesControllerComponents())

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
      Mockito.verify(responseConsumer).addResponse(any(): JsValue, any(): LocalDate)
    }

    "return OK and write to logs for POST for quarantined upload" in {
      Given("a request containing correctly formatted JSON and a valid download URL for quarantined upload")
      val responseConsumer = mock[ResponseConsumer]
      val controller = new ListenerController(responseConsumer, stubMessagesControllerComponents())

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
      Mockito.verify(responseConsumer).addResponse(any(): JsValue, any(): LocalDate)
    }

    "return BadRequest for a file that contains invalid body" in {
      Given("a request containing body content that cannot be parsed as JSON")
      val controller = new ListenerController(mock[ResponseConsumer], stubMessagesControllerComponents())

      val request = FakeRequest().withTextBody("This is not JSON")

      When("the POST endpoint is called")
      val response: Future[Result] = controller.listen()(request)

      Then("the service should return InternalServerError")
      status(response) shouldBe 400
    }
  }
}
