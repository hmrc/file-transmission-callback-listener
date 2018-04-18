package model

import java.time.LocalDate
import play.api.libs.json.{Format, JsValue, Json}

case class ResponseLog(currentDate: LocalDate,
                       responses: Seq[JsValue])

object ResponseLog {
  implicit val formats: Format[ResponseLog] = Json.format[ResponseLog]
}