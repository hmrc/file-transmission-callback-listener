package utils

import java.time.LocalDate

import model.ResponseLog
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue}
import utils.logging.WithFileReference.withFileReferenceLogged

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Queue

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses(): ResponseLog

  def lookupResponseForReference(reference: String): Option[JsValue]
}

class InMemoryResponseConsumer(private var responsesDate: LocalDate, maximumQueueLength: Int = 10000)
    extends ResponseConsumer {

  private val responseMap = TrieMap[String, JsValue]()

  private var responseQueue: Queue[JsValue] = Queue.empty[JsValue]

  override def addResponse(response: JsValue, today: LocalDate): Unit =
    withFileReferenceLogged(response) {
      checkAndRefreshCache(today)
      dropOldestOnes()
      val refefence: Option[String] = getReference(response)

      refefence match {
        case Some(existingReference) =>
          responseMap.put(existingReference, response)
          synchronized {
            responseQueue = responseQueue.enqueue(response)
          }
          Logger.info(s"Added response: [$response].")
        case None =>
          Logger.warn(s"Unparseable callback $response")
      }
    }

  private def getReference(response: JsValue) =
    response match {
      case JsObject(fields) => fields.get("reference").flatMap(_.asOpt[String])
      case _                => None
    }

  // NOTE: Can return stale results from a previous day -- iff the day has changed and no new responses have yet been received for the current day.
  override def retrieveResponses(): ResponseLog =
    synchronized {
      ResponseLog(responsesDate, responseQueue)
    }

  private def dropOldestOnes(): Unit =
    synchronized {
      while (responseQueue.length >= maximumQueueLength) {
        val (element, newQueue) = responseQueue.dequeue
        responseQueue = newQueue
        getReference(element).map(responseMap.remove)
      }
    }

  private def checkAndRefreshCache(today: LocalDate): Unit =
    if (today.isAfter(responsesDate)) {
      responsesDate = today
      responseMap.clear()
      synchronized {
        responseQueue = Queue.empty[JsValue]
      }
      Logger.info(s"Resetting responses for new day: [$today].")
    }

  override def lookupResponseForReference(reference: String): Option[JsValue] =
    responseMap.get(reference)
}
