package connectors

import java.net.URL

import scala.concurrent.Future

trait FileHashRetriever {
  def fileHash(url: URL): Future[String]
}
