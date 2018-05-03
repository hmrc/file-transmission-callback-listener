package utils

import model.ResponseLog
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicReference

import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import utils.logging.WithFileReference.withFileReferenceLogged

import scala.collection.concurrent.TrieMap

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses(): ResponseLog

  def lookupResponseForReference(reference: String): Option[JsValue]
}

class InMemoryResponseConsumer(private var responsesDate: LocalDate) extends ResponseConsumer {

  private val responseMap = TrieMap[String, JsValue]()

  private var responseList: List[JsValue] = Nil

  override def addResponse(response: JsValue, today: LocalDate): Unit =
    withFileReferenceLogged(response) {
      checkAndRefreshCache(today)
      val refefence: Option[String] = response match {
        case JsObject(fields) => fields.get("reference").flatMap(_.asOpt[String])
        case _                => None
      }

      refefence match {
        case Some(existingReference) =>
          responseMap.put(existingReference, response)
          synchronized {
            responseList = response :: responseList
          }
          Logger.info(s"Added response: [$response].")
        case None =>
          Logger.warn(s"Unparseable callback $response")
      }
    }

  // NOTE: Can return stale results from a previous day -- iff the day has changed and no new responses have yet been received for the current day.
  override def retrieveResponses(): ResponseLog =
    synchronized {
      ResponseLog(responsesDate, responseList)
    }

  private def checkAndRefreshCache(today: LocalDate): Unit =
    if (today.isAfter(responsesDate)) {
      responsesDate = today
      responseMap.clear()
      synchronized {
        responseList = Nil
      }
      Logger.info(s"Resetting responses for new day: [$today].")
    }

  override def lookupResponseForReference(reference: String): Option[JsValue] =
    responseMap.get(reference)
}
