package model

import java.net.URL

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object JsonHelpers {
  implicit val urlFormats = new Format[URL] {
    override def reads(json: JsValue): JsResult[URL] = {
      json.validate[String] match {
        case JsSuccess(s, _) => Try(new URL(s)) match {
          case Success(url) => JsSuccess(url)
          case Failure(error) => JsError(s"Unable to convert to valid URL: $s. Error: ${error.getMessage}")
        }
        case error: JsError => error
      }
    }

    override def writes(o: URL): JsValue = JsString(o.toString)
  }
}
