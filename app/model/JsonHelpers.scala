package model

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._

object JsonHelpers {
  implicit val datetimeFormats = new Format[DateTime] {
    private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")

    override def writes(dateTime: DateTime): JsValue = JsString(formatter.print(dateTime))

    override def reads(json: JsValue): JsResult[DateTime] = json.validate[String].map(formatter.parseDateTime)
  }
}
