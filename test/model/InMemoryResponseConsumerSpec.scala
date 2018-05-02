package model

import java.time.LocalDate
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.InMemoryResponseConsumer

class InMemoryResponseConsumerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {
  private val initialResponses: List[JsValue] = List(
    Json.parse("""{"reference": "my-first-reference", "url": "http://url.one", "fileStatus": "READY"}"""),
    Json.parse("""{"reference": "my-second-reference",  "details": "This file had a virus", "fileStatus": "FAILED"}"""),
    Json.parse("""{"reference": "my-third-reference", "url": "http://url.three", "fileStatus": "READY"}""")
  )

  private val initialDate = LocalDate.parse("2018-03-16")

  "InMemoryResponseConsumer" should {
    "initialize with the date and populated list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")

      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, initialResponses.reverse)
    }

    "initialize with the date and empty list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")
      val initialDate = LocalDate.parse("2018-03-16")
      val consumer    = new InMemoryResponseConsumer(initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, Nil)
    }

    "append event to log if current date is same day as log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("a successful event event with the same date as the log is added")
      val newResponse =
        Json.parse("""{"reference": "my-fourth-reference", "url": "http://url.four", "fileStatus": "READY"}""")
      consumer.addResponse(newResponse, initialDate)

      Then("the expected response log should be returned")
      val updatedResponses = initialResponses :+ newResponse
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, updatedResponses.reverse)
    }

    "reset whole queue if event current date is greater than log day" in {
      Given("an InMemoryResponseConsumer with a date")
      val consumer = new InMemoryResponseConsumer(initialDate)

      And("there are some initial responses")
      initialResponses.foreach(response => consumer.addResponse(response, initialDate))

      When("a successful event with the next date as the log is added")
      val newResponse =
        Json.parse("""{"reference": "my-fourth-reference", "url": "http://url.four", "fileStatus": "READY"}""")
      val newDate = LocalDate.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(newDate, List(newResponse))
    }
  }
}
