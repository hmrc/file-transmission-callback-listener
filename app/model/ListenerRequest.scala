package model

import java.net.URL

import play.api.libs.json.{Format, Json}

case class ListenerRequest(reference: String, downloadUrl: URL)

object ListenerRequest {
  import JsonHelpers.urlFormats

  implicit val listenerRequestFormats: Format[ListenerRequest] = Json.format[ListenerRequest]
}
