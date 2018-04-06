package model

import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}

case class ResponseLog(currentDate: DateTime,
                       successfulResponses: List[ListenerResponseSuccessfulUpload],
                       quarantineResponses: List[QuarantinedFile])

object ResponseLog {
  import JsonHelpers.datetimeFormats
  
  implicit val formats: Format[ResponseLog] = Json.format[ResponseLog]
}