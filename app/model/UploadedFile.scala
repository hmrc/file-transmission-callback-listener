package model

import java.net.URL

import play.api.libs.json.{Format, Json}

case class UploadedFile(reference: String, downloadUrl: URL)

object UploadedFile {
  import JsonHelpers.urlFormats

  implicit val uploadedFileFormats: Format[UploadedFile] = Json.format[UploadedFile]
}

case class QuarantinedFile(reference: String, details: String)

object QuarantinedFile {
  implicit val quarantinedFileFormats: Format[QuarantinedFile] = Json.format[QuarantinedFile]
}