package utils

import model.ResponseLog
import java.time.LocalDate

import play.api.Logger
import play.api.libs.json.JsValue
import utils.logging.WithFileReference.withFileReferenceLogged

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses: ResponseLog
}

class InMemoryResponseConsumer(private var responsesDate: LocalDate) extends ResponseConsumer {

  private var responses: List[JsValue] = Nil

  override def addResponse(response: JsValue, today: LocalDate): Unit =
    withFileReferenceLogged(response) {
      synchronized {
        checkAndRefreshCache(today)
        responses = response :: responses
      }
      Logger.info(s"Added response: [$response].")
    }

  // NOTE: Can return stale results from a previous day -- iff the day has changed and no new responses have yet been received for the current day.
  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, responses)

  private def checkAndRefreshCache(today: LocalDate): Unit =
    if (today.isAfter(responsesDate)) {
      responsesDate = today
      responses     = List.empty[JsValue]
      Logger.info(s"Resetting responses for new day: [$today].")
    }
}
