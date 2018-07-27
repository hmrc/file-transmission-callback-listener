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

package model

import java.time.LocalDate
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.InMemoryResponseConsumer

class InMemoryResponseConsumerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {
  private val initialResponses: List[JsValue] = List(
    Json.parse("""{"fileReference": "my-first-reference", "batchId": "http://url.one", "outcome": "READY"}"""),
    Json.parse(
      """{"fileReference": "my-second-reference",  "details": "This file had a virus", "outcome": "FAILED"}"""),
    Json.parse("""{"fileReference": "my-third-reference", "batchId": "http://url.three", "outcome": "READY"}""")
  )

  private val initialDate = LocalDate.parse("2018-03-16")

  "InMemoryResponseConsumer" should {
    "initialize with the date and populated list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")

      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      Then("the expected response log should be returned")
      consumer.retrieveResponses() shouldBe ResponseLog(initialDate, initialResponses)
    }

    "initialize with the date and empty list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")
      val initialDate = LocalDate.parse("2018-03-16")
      val consumer    = new InMemoryResponseConsumer(initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses() shouldBe ResponseLog(initialDate, Nil)
    }

    "append event to log if current date is same day as log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("a successful event event with the same date as the log is added")
      val newResponse =
        Json.parse("""{"fileReference": "my-fourth-reference", "batchId": "http://url.four", "outcome": "READY"}""")
      consumer.addResponse(newResponse, initialDate)

      Then("the expected response log should be returned")
      val updatedResponses = initialResponses :+ newResponse
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, updatedResponses)
    }

    "allow to lookup for added events by reference" in {
      Given("an InMemoryResponseConsumer with a date")
      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("a successful event with the next date as the log is added")
      val newResponse =
        Json.parse("""{"fileReference": "my-fourth-reference", "batchId": "http://url.four", "outcome": "READY"}""")
      val newDate = LocalDate.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.lookupResponseForReference("my-fourth-reference")    shouldBe Some(newResponse)
      consumer.lookupResponseForReference("non-existent-reference") shouldBe None
    }

    "purge the oldest messages if reached queue size limit" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val consumer = new InMemoryResponseConsumer(initialDate, maximumQueueLength = 3)

      And("there are some initial responses - up to the limit")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("the new response is added")
      val newResponse =
        Json.parse("""{"fileReference": "my-fourth-reference", "batchId": "http://url.four", "outcome": "READY"}""")
      consumer.addResponse(newResponse, initialDate)

      Then("response list should contain the most recently added response")
      consumer.retrieveResponses().responses                               should contain(newResponse)
      consumer.lookupResponseForReference("my-fourth-reference").isDefined shouldBe true

      And("response list shouldn't contain the erliest added response")
      consumer.retrieveResponses().responses shouldNot contain(initialResponses.head)
      consumer.lookupResponseForReference("my-first-reference").isDefined shouldBe false
    }

    "reset whole queue if event current date is greater than log day" in {
      Given("an InMemoryResponseConsumer with a date")
      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("a successful event with the next date as the log is added")
      val newResponse =
        Json.parse("""{"fileReference": "my-fourth-reference", "batchId": "http://url.four", "outcome": "READY"}""")
      val newDate = LocalDate.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses() shouldBe ResponseLog(newDate, List(newResponse))
    }
  }
}
