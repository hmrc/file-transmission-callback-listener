package utils

import model.{ListenerResponseSuccessfulUpload, QuarantinedFile, UploadedFile}
import play.api.Logger
import play.api.libs.json.JsValue

trait CallbackConsumer {
  def logSuccessfulResponse(listenerResponse: ListenerResponseSuccessfulUpload): Unit
  def logQuarantinedResponse(quarantinedFile: QuarantinedFile): Unit
  def logHashError(listenerRequest: UploadedFile, errorMsg: String): Unit
  def logInvalidJson(json: JsValue)
  def logInvalidBody(body: String)
}

class PlayCallbackConsumer extends CallbackConsumer {
  override def logSuccessfulResponse(listenerResponse: ListenerResponseSuccessfulUpload): Unit = {
    Logger.info(s"File upload notification received on callback URL. File reference: ${listenerResponse.reference}, " +
      s"file download URL: ${listenerResponse.downloadUrl}, file hash: ${listenerResponse.hash}")
  }

  override def logQuarantinedResponse(quarantinedFile: QuarantinedFile): Unit = {
    Logger.info(s"File quarantine notification received on callback URL. File reference: ${quarantinedFile.reference}, " +
      s"file error: ${quarantinedFile.details}")
  }

  override def logHashError(listenerRequest: UploadedFile, errorMsg: String): Unit = {
    Logger.error(s"Unable to retrieve hash of file in callback. File reference: ${listenerRequest.reference}, " +
      s"file download URL: ${listenerRequest.downloadUrl}, error: $errorMsg")
  }

  override def logInvalidJson(json: JsValue): Unit = {
    Logger.error(s"JSON of callback not in expected format, JSON body is: $json")
  }

  override def logInvalidBody(body: String): Unit = {
    Logger.error(s"Request body cannot be parsed as JSON, request body is: ${body.toString}")
  }

}
