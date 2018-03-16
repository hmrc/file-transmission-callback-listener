package connectors.aws

import java.net.URL
import java.security.MessageDigest
import javax.inject.Inject

import connectors.FileHashRetriever
import play.api.http.Status
import play.api.libs.ws.WSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class S3FileHashRetriever @Inject()(wsClient: WSClient)
                                   (implicit ec: ExecutionContext) extends FileHashRetriever {

  override def fileHash(url: URL): Future[String] = {
    wsClient.url(url.toString).get() flatMap { response =>
      response.status match {
        case Status.OK => getMD5Hash(response.bodyAsBytes.toArray) match {
          case Success(hash) =>
            Future.successful(hash)
          case Failure(error) =>
            Future.failed(new Exception(s"Unable to successfully create MD5 hash of file: ${error.getMessage}"))
        }
        case statusNotOk =>
          Future.failed(new Exception(s"File not successfully retrieved from S3, status was: $statusNotOk"))
      }
    }
  }

  private def getMD5Hash(bytes: Array[Byte]): Try[String] = {
    val md5 = Try(MessageDigest.getInstance("MD5").digest(bytes))
    md5.map(_.map("%02x".format(_)).mkString)
  }
}