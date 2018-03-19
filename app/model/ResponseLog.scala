package model

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

case class ResponseLog(currentDate: DateTime, responses: List[ListenerResponse])

object ResponseLog {
  import JsonHelpers.datetimeFormats
  
  implicit val formats: Format[ResponseLog] = Json.format[ResponseLog]
}