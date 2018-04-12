package utils

import javax.inject.Singleton

import model.ResponseLog
import org.joda.time.DateTime
import play.api.libs.json.JsValue

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: DateTime): Unit


  def retrieveResponses: ResponseLog
}

@Singleton
class InMemoryResponseConsumer(private val initialDate: DateTime,
                               private val initialResponses: List[JsValue]) extends ResponseConsumer {

  private var responsesDate: DateTime = initialDate
  private var responses: List[JsValue] = initialResponses

  override def addResponse(response: JsValue, currentDate: DateTime): Unit = {
    synchronized {
      checkAndRefreshCache(currentDate)
      responses = responses :+ response
    }
  }

  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, responses)

  private def checkAndRefreshCache(currentDate: DateTime): Unit = {
    if (currentDate.dayOfYear().get() > responsesDate.dayOfYear().get()) {
      responsesDate = currentDate
      responses = List.empty
    }
  }
}