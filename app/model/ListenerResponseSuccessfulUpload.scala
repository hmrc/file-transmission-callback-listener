package model

import java.net.URL

import play.api.libs.json.{Format, Json}

case class ListenerResponseSuccessfulUpload(reference: String, downloadUrl: URL, hash: String)

object ListenerResponseSuccessfulUpload {
  import JsonHelpers.urlFormats

  implicit val responseFormat: Format[ListenerResponseSuccessfulUpload] = Json.format[ListenerResponseSuccessfulUpload]
}