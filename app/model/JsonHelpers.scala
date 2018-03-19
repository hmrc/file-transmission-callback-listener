package model

import java.net.URL

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
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

  implicit val datetimeFormats = new Format[DateTime] {
    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override def writes(dateTime: DateTime): JsValue = JsString(formatter.print(dateTime))

    override def reads(json: JsValue): JsResult[DateTime] = json.validate[String].map(formatter.parseDateTime)
  }
}
