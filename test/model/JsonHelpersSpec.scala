package model

import java.net.URL

import org.scalatest.{GivenWhenThen, Matchers}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess}
import uk.gov.hmrc.play.test.UnitSpec

class JsonHelpersSpec extends UnitSpec with Matchers with GivenWhenThen {
  "JsonHelpers" should {
    "write URL as JSON" in {
      Given("a URL object")
      val url = new URL("http://some.url")

      When("the URL is written as JSON")
      val result = JsonHelpers.urlFormats.writes(url)

      Then("the URL should be written as a string")
      result shouldBe JsString("http://some.url")
    }

    "read valid URL string as JSON" in {
      Given("a JsString of an valid URL")
      val jsString = JsString("http://some.url")

      When("the string is read as JSON")
      val result = JsonHelpers.urlFormats.reads(jsString)

      Then("a wrapped URL should be returned")
      result shouldBe JsSuccess(new URL("http://some.url"))
    }

    "read invalid URL string as failed JSON" in {
      Given("a JsString of an valid URL")
      val jsString = JsString("not-a-url")

      When("the string is read as JSON")
      val result = JsonHelpers.urlFormats.reads(jsString)

      Then("a wrapped error should be returned")
      val validationError = ValidationError(List("Unable to convert to valid URL: not-a-url. Error: no protocol: not-a-url"))
      val error = JsError(validationError)
      result shouldBe error
    }

    "read values that are not string as failed JSON" in {
      Given("a JsValue that is not a string")
      val jsNumber = JsNumber(10)

      When("the value is read as JSON")
      val result = JsonHelpers.urlFormats.reads(jsNumber)

      Then("a wrapped error should be returned")
      val validationError = ValidationError(List("error.expected.jsstring"))
      val error = JsError(validationError)
      result shouldBe error
    }
  }
}
