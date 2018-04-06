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
      val successfulUploads = List(
        ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
      )

      val quarantinedUploads = List(
        QuarantinedFile("my-third-reference", "This file has one nasty virus"),
        QuarantinedFile("my-fourth-reference", "This file has two nasty viruses")
      )

      val consumer = new InMemoryResponseConsumer(successfulUploads, quarantinedUploads, initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, successfulUploads, quarantinedUploads)
    }

    "initialize with the date and empty list passed in" in {
      When("an InMemoryResponseConsumer is created with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val consumer = new InMemoryResponseConsumer(Nil, Nil, initialDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, Nil, Nil)
    }

    "append event to successful log if current date is same day as log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val successfulUploads = List(
        ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
      )
      val quarantinedUploads = List(
        QuarantinedFile("my-third-reference", "This file has one nasty virus"),
        QuarantinedFile("my-fourth-reference", "This file has two nasty viruses")
      )
      val consumer = new InMemoryResponseConsumer(successfulUploads, quarantinedUploads, initialDate)

      When("a successful event event with the same date as the log is added")
      val newResponse = ListenerResponseSuccessfulUpload("my-this-reference", new URL("http://url.three"), "33333")
      consumer.addResponse(newResponse, initialDate)

      Then("the expected response log should be returned")
      val updatedResponse = successfulUploads :+ newResponse
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, updatedResponse, quarantinedUploads)
    }

    "append event to quarantine log if current date is same day as log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val successfulUploads = List(
        ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
      )
      val quarantinedUploads = List(
        QuarantinedFile("my-third-reference", "This file has one nasty virus"),
        QuarantinedFile("my-fourth-reference", "This file has two nasty viruses")
      )
      val consumer = new InMemoryResponseConsumer(successfulUploads, quarantinedUploads, initialDate)

      When("a successful event event with the same date as the log is added")
      val newResponse = QuarantinedFile("my-fifth-reference", "This file has three nasty viruses")
      consumer.addResponse(newResponse, initialDate)

      Then("the expected response log should be returned")
      val updatedResponse = quarantinedUploads :+ newResponse
      consumer.retrieveResponses shouldBe ResponseLog(initialDate, successfulUploads, updatedResponse)
    }

    "reset whole queue if successful current date is greater than log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val successfulUploads = List(
        ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
      )

      val quarantinedUploads = List(
        QuarantinedFile("my-third-reference", "This file has one nasty virus"),
        QuarantinedFile("my-fourth-reference", "This file has two nasty viruses")
      )

      val consumer = new InMemoryResponseConsumer(successfulUploads, quarantinedUploads, initialDate)

      When("a successful event with the next date as the log is added")
      val newResponse = ListenerResponseSuccessfulUpload("my-this-reference", new URL("http://url.three"), "33333")
      val newDate = DateTime.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(newDate, List(newResponse), Nil)
    }

    "reset whole queue if successful quarantine date is greater than log day" in {
      Given("an InMemoryResponseConsumer with a date and a populated list")
      val initialDate = DateTime.parse("2018-03-16")
      val successfulUploads = List(
        ListenerResponseSuccessfulUpload("my-first-reference", new URL("http://url.one"), "11111"),
        ListenerResponseSuccessfulUpload("my-second-reference", new URL("http://url.two"), "22222")
      )

      val quarantinedUploads = List(
        QuarantinedFile("my-third-reference", "This file has one nasty virus"),
        QuarantinedFile("my-fourth-reference", "This file has two nasty viruses")
      )

      val consumer = new InMemoryResponseConsumer(successfulUploads, quarantinedUploads, initialDate)

      When("a quarantine event with the next date as the log is added")
      val newResponse = QuarantinedFile("my-fifth-reference", "This file has three nasty viruses")
      val newDate = DateTime.parse("2018-03-17")
      consumer.addResponse(newResponse, newDate)

      Then("the expected response log should be returned")
      consumer.retrieveResponses shouldBe ResponseLog(newDate, Nil, List(newResponse))
    }
  }
}
