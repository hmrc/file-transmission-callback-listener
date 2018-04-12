package model

import org.joda.time.DateTime
import play.api.libs.json.{Format, JsValue, Json}

case class ResponseLog(currentDate: DateTime,
                       responses: List[JsValue])

object ResponseLog {
  import JsonHelpers.datetimeFormats
  
  implicit val formats: Format[ResponseLog] = Json.format[ResponseLog]
}