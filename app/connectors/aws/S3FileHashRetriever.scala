package connectors.aws

import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject

import connectors.FileHashRetriever
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class S3FileHashRetriever @Inject()(httpClient: HttpClient)
                                   (implicit ec: ExecutionContext) extends FileHashRetriever {

  override def fileHash(url: URL): Future[String] = {
    implicit val hc = HeaderCarrier()
    httpClient.GET(url.toString) flatMap { response =>
      getMD5Hash(response.body.getBytes()) match {
        case Success(hash) =>
          Future.successful(hash)
        case Failure(error) =>
          Future.failed(new Exception(s"Unable to successfully create MD5 hash of file: ${error.getMessage}"))
      }
    }
  }

  private def getMD5Hash(bytes: Array[Byte]): Try[String] = {
    val md5 = Try(MessageDigest.getInstance("MD5").digest(bytes))
    md5.map(_.map("%02x".format(_)).mkString)
  }
}