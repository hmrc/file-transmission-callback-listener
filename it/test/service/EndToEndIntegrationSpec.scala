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

package service

import java.time.LocalDate

import org.scalatest.GivenWhenThen
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import play.api.test.{FakeHeaders, FakeRequest, Helpers}
import play.api.test.Helpers._

class EndToEndIntegrationSpec extends AnyWordSpecLike with Matchers with GuiceOneAppPerSuite with GivenWhenThen {

  "End To End Integration" should {
    "post 3 times to ListenerController and poll PollController for response" in {
      Given("the service receives 3 POSTs to /listen")
      for (i <- 1 to 3) {
        val listenRequest =
          FakeRequest(Helpers.POST, "/file-transmission-callback-listener/listen", FakeHeaders(), postBodyJson(i))

        val listenResponseF = route(app, listenRequest).get
        status(listenResponseF)        shouldBe 200
        contentAsJson(listenResponseF) shouldBe postBodyJson(i)
      }

      When("the service is polled on /poll")
      val pollRequest = FakeRequest(Helpers.GET, "/file-transmission-callback-listener/poll")

      val pollResponseF = route(app, pollRequest).get
      status(pollResponseF) shouldBe 200

      Then("the service should return a record of the current date and the 3 POSTs")
      contentAsJson(pollResponseF) shouldBe Json.obj(
        "currentDate" -> LocalDate.now.toString,
        "responses" -> Json.arr(
          postBodyJson(1),
          postBodyJson(2),
          postBodyJson(3)
        )
      )
    }

    "it's possible to lookup for created events" in {
      Given("the service receives 3 POSTs to /listen")
      for (i <- 1 to 3) {
        val listenRequest =
          FakeRequest(Helpers.POST, "/file-transmission-callback-listener/listen", FakeHeaders(), postBodyJson(i))

        val listenResponseF = route(app, listenRequest).get
        status(listenResponseF)        shouldBe 200
        contentAsJson(listenResponseF) shouldBe postBodyJson(i)
      }

      When("looking up for details of specific event")
      val pollRequest = FakeRequest(Helpers.GET, "/file-transmission-callback-listener/poll/my-reference-1")

      Then("we should get details of this event")
      val pollResponseF = route(app, pollRequest).get
      status(pollResponseF) shouldBe 200

      contentAsJson(pollResponseF) shouldBe postBodyJson(1)
    }

    "looking up for non existing events should end with HTTP 404 error" in {
      Given("the service receives 3 POSTs to /listen")
      for (i <- 1 to 3) {
        val listenRequest =
          FakeRequest(Helpers.POST, "/file-transmission-callback-listener/listen", FakeHeaders(), postBodyJson(i))

        val listenResponseF = route(app, listenRequest).get
        status(listenResponseF)        shouldBe 200
        contentAsJson(listenResponseF) shouldBe postBodyJson(i)
      }

      When("looking up for details of non-existent event")
      val pollRequest = FakeRequest(Helpers.GET, "/file-transmission-callback-listener/poll/non-existent")

      Then("we should get HTTP 404 error")
      val pollResponseF = route(app, pollRequest).get
      status(pollResponseF) shouldBe 404
    }

  }

  private def postBodyJson(i: Int): JsObject =
    Json.obj(
      "fileReference" -> s"my-reference-$i",
      "downloadUrl"   -> s"http://my$i.download.url"
    )
}
