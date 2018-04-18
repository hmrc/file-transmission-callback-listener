package utils

import javax.inject.Singleton

import model.ResponseLog
import java.time.LocalDate
import play.api.libs.json.JsValue

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses: ResponseLog
}

@Singleton
class InMemoryResponseConsumer(initialDate: LocalDate,
                               initialResponses: Seq[JsValue]) extends ResponseConsumer {

  private var responsesDate: LocalDate = initialDate
  private var responses: Seq[JsValue] = initialResponses

  override def addResponse(response: JsValue, currentDate: LocalDate): Unit = {
    synchronized {
      checkAndRefreshCache(currentDate)
      responses = responses :+ response
    }
  }

  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, responses)

  private def checkAndRefreshCache(currentDate: LocalDate): Unit = {
    if (currentDate.isAfter(responsesDate)) {
      responsesDate = currentDate
      responses = Seq.empty
    }
  }
}