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

import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import play.api.test.Helpers._

class PollControllerISpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite with GivenWhenThen {
  "PollController" should {
    "clear all cached responses" in {
      Given("the service receives 3 POSTs to /listen")
      for (i <- 1 to 3) {
        val listenRequest = FakeRequest(Helpers.POST, "/file-transmission-callback-listener/listen", FakeHeaders(), postBodyJson(i))

        val listenResponseF = route(app, listenRequest).get
        status(listenResponseF)        shouldBe 200
        contentAsJson(listenResponseF) shouldBe postBodyJson(i)
      }

      And("the cache is populated")
      val pollResponseF1 = route(app, FakeRequest(Helpers.GET, "/file-transmission-callback-listener/poll")).get

      contentAsJson(pollResponseF1) shouldBe Json.obj(
        "currentDate" -> LocalDate.now.toString,
        "responses"   -> Json.arr(postBodyJson(1), postBodyJson(2), postBodyJson(3))
      )

      When("the service is cleared on /clear")
      val clearRequest = FakeRequest(Helpers.GET, "/file-transmission-callback-listener/clear")
      val clearResponseF = route(app, clearRequest).get

      Then("the response is a 303, redirecting to /poll")
      status(clearResponseF) shouldBe 303

      And("then subsequent calls to /poll should return an empty array")

      val pollRequest = FakeRequest(Helpers.GET, "/file-transmission-callback-listener/poll")
      val pollResponseF = route(app, pollRequest).get

      status(pollResponseF) shouldBe 200

      contentAsJson(pollResponseF) shouldBe Json.obj(
        "currentDate" -> LocalDate.now.toString,
        "responses"   -> Json.arr()
      )
    }
  }

  private def postBodyJson(i: Int): JsObject =
    Json.obj(
      "fileReference" -> s"my-reference-$i",
      "batchId"       -> s"batchId123",
      "outcome"       ->  "SUCCESS"
    )
}
