package utils

import model.{ListenerRequest, ListenerResponse}
import play.api.Logger
import play.api.libs.json.JsValue

trait CallbackConsumer {
  def logSuccessfulResponse(listenerResponse: ListenerResponse): Unit
  def logHashError(listenerRequest: ListenerRequest, errorMsg: String): Unit
  def logInvalidJson(json: JsValue)
  def logInvalidBody(body: String)
}

class PlayCallbackConsumer extends CallbackConsumer {
  override def logSuccessfulResponse(listenerResponse: ListenerResponse): Unit = {
    val logMsg = s"File upload notification received on callback URL. File reference: ${listenerResponse.reference}, " +
      s"file download URL: ${listenerResponse.downloadUrl}, file hash: ${listenerResponse.hash}"
    Logger.info(logMsg)
  }

  override def logHashError(listenerRequest: ListenerRequest, errorMsg: String): Unit = {
    val logMsg = s"Unable to retrieve hash of file in callback. File reference: ${listenerRequest.reference}, " +
      s"file download URL: ${listenerRequest.downloadUrl}, error: $errorMsg"
    Logger.error(logMsg)
  }

  override def logInvalidJson(json: JsValue): Unit = {
    Logger.error(s"JSON of callback not in expected format, JSON body is: $json")
  }

  override def logInvalidBody(body: String): Unit = {
    Logger.error(s"Request body cannot be parsed as JSON, request body is: ${body.toString}")
  }
}
