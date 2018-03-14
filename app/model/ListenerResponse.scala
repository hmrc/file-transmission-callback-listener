package model

import java.net.URL

import play.api.libs.json.{Format, Json}

case class ListenerResponse(reference: String, downloadUrl: URL, hash: String)

object ListenerResponse {
  import JsonHelpers.urlFormats

  implicit val responseFormat: Format[ListenerResponse] = Json.format[ListenerResponse]
}