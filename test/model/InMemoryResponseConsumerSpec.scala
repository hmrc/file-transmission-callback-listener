package model

import java.net.URL

import org.joda.time.DateTime
import org.scalatest.{GivenWhenThen, Matchers}
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec
import utils.InMemoryResponseConsumer

class InMemoryResponseConsumerSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {
  "InMemoryResponseConsumer" should {
    "initialize with the date and populated list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val responses = List(
        ListenerResponse("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponse("my-second-reference", new URL("http://url.two"), "22222")
      )

      val consumer = new InMemoryResponseConsumer(responses, initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, responses)
    }

    "initialize with the date and empty list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val consumer = new InMemoryResponseConsumer(Nil, initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, Nil)
    }

    "append event to log if current date is same day as log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val responses = List(
        ListenerResponse("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponse("my-second-reference", new URL("http://url.two"), "22222")
      )
      val consumer = new InMemoryResponseConsumer(responses, initialDate)

      When("an event with the same date as the log is added")
      val newResponse = ListenerResponse("my-this-reference", new URL("http://url.three"), "33333")
      consumer.addResponse(newResponse, initialDate)

      Then("the expected response log should be returned")
      val updatedResponse = responses :+ newResponse
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, updatedResponse)
    }

    "reset queue if current date is greater than log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val responses = List(
        ListenerResponse("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponse("my-second-reference", new URL("http://url.two"), "22222")
      )
      val consumer = new InMemoryResponseConsumer(responses, initialDate)

      When("an event with the next date as the log is added")
      val newResponse = ListenerResponse("my-this-reference", new URL("http://url.three"), "33333")
      val newDate = DateTime.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(newDate, List(newResponse))
    }
  }
}
