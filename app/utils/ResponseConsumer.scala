package utils

import javax.inject.Singleton
import model.ResponseLog
import java.time.LocalDate

import play.api.Logger
import play.api.libs.json.JsValue

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses: ResponseLog
}

@Singleton
class InMemoryResponseConsumer(private var responsesDate: LocalDate,
                               private var responses: Seq[JsValue]) extends ResponseConsumer {

  override def addResponse(response: JsValue, today: LocalDate): Unit = {
    synchronized {
      checkAndRefreshCache(today)
      responses = responses :+ response
    }
    Logger.info(s"Added response: [$response].")
  }

  // NOTE: Can return stale results from a previous day -- iff the day has changed and no new responses have yet been received for the current day.
  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, responses)

  private def checkAndRefreshCache(today: LocalDate): Unit = {
    if (today.isAfter(responsesDate)) {
      responsesDate = today
      responses = Seq.empty
      Logger.info(s"Resetting responses for new day.")
    }
  }
}