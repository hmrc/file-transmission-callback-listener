package connectors.aws

import java.net.URL

import akka.util.ByteString
import org.mockito.Mockito
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{GivenWhenThen, Matchers}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class S3FileHashRetrieverSpec extends UnitSpec with Matchers with GivenWhenThen with MockitoSugar {
  "S3FileHashRetriever" should {
    "retrieve a file from a URL via GET, and hash the byte content using MD5 hashing" in {
      Given("a valid file URL")
      val url = "http://some.valid.url"
      val fileContents = "test-string"
      val byteString = ByteString(fileContents)

      val wsResponse = mock[WSResponse]
      Mockito.when(wsResponse.status).thenReturn(200)
      Mockito.when(wsResponse.bodyAsBytes).thenReturn(byteString)

      val wsRequest = mock[WSRequest]
      Mockito.when(wsRequest.get()).thenReturn(Future(wsResponse))

      val client = mock[WSClient]
      Mockito.when(client.url(url)).thenReturn(wsRequest)

      val connector = new S3FileHashRetriever(client)

      When("the retriever is called")
      val hash: String = Await.result(connector.fileHash(new URL(url)), 2.seconds)

      Then("the file should be retrieved via GET")
      Mockito.verify(client).url(url)
      Mockito.verify(wsRequest).get()

      And("the MD5 hash returned as a string")
      hash shouldBe "661f8009fa8e56a9d0e94a0a644397d7"
    }

    "return expected error if retrieve a file from a URL via GET returns status other than OK" in {
      Given("a file URL that fails")
      val url = "http://some.invalid.url"

      val wsResponse = mock[WSResponse]
      Mockito.when(wsResponse.status).thenReturn(403)

      val wsRequest = mock[WSRequest]
      Mockito.when(wsRequest.get()).thenReturn(Future(wsResponse))

      val client = mock[WSClient]
      Mockito.when(client.url(url)).thenReturn(wsRequest)

      When("the retriever is called")
      val connector = new S3FileHashRetriever(client)

      ScalaFutures.whenReady(connector.fileHash(new URL(url)).failed) { error =>
        Then("the file should be retrieved via GET")
        Mockito.verify(client).url(url)
        Mockito.verify(wsRequest).get()

        And("a wrapped error returned")
        error shouldBe a[Exception]
        error.getMessage shouldBe "File not successfully retrieved from S3, status was: 403"
      }
    }

    "return expected error if retrieve a file from a URL via GET fails" in {
      Given("a file URL that fails")
      val url = "http://some.invalid.url"

      val wsRequest = mock[WSRequest]
      Mockito.when(wsRequest.get()).thenReturn(Future.failed(new Exception("This is an expected invalid url exception")))

      val client = mock[WSClient]
      Mockito.when(client.url(url)).thenReturn(wsRequest)

      When("the retriever is called")
      val connector = new S3FileHashRetriever(client)

      ScalaFutures.whenReady(connector.fileHash(new URL(url)).failed) { error =>
        Then("the file should be retrieved via GET")
        Mockito.verify(client).url(url)
        Mockito.verify(wsRequest).get()

        And("a wrapped error returned")
        error shouldBe a[Exception]
        error.getMessage shouldBe "This is an expected invalid url exception"
      }
    }
  }
}
