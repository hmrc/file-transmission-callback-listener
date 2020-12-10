/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import java.time.LocalDate

import model.ResponseLog
import play.api.Logging
import play.api.libs.json.{JsObject, JsValue}
import utils.logging.WithFileReference.withFileReferenceLogged

import scala.collection.concurrent.TrieMap
import scala.collection.immutable.Queue

trait ResponseConsumer {
  def addResponse(response: JsValue, currentDate: LocalDate): Unit

  def retrieveResponses(): ResponseLog

  def lookupResponseForReference(reference: String): Option[JsValue]

  def clear(): Unit
}

class InMemoryResponseConsumer(private var responsesDate: LocalDate, maximumQueueLength: Int = 10000)
    extends ResponseConsumer with Logging {

  private val responseMap = TrieMap[String, JsValue]()

  private var responseQueue: Queue[JsValue] = Queue.empty[JsValue]

  override def addResponse(response: JsValue, today: LocalDate): Unit =
    withFileReferenceLogged(response) {
      checkAndRefreshCache(today)
      dropOldestOnes()
      val reference: Option[String] = getReference(response)

      reference match {
        case Some(existingReference) =>
          responseMap.put(existingReference, response)
          synchronized {
            responseQueue = responseQueue.enqueue(response)
          }
          logger.info(s"Added response: [$response].")
        case None =>
          logger.warn(s"Unparseable callback $response")
      }
    }

  private def getReference(response: JsValue) =
    response match {
      case JsObject(fields) => fields.get("fileReference").flatMap(_.asOpt[String])
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
      clear()
      logger.info(s"Resetting responses for new day: [$today].")
    }

  override def lookupResponseForReference(reference: String): Option[JsValue] =
    responseMap.get(reference)

  override def clear(): Unit = {
    synchronized {
      responseMap.clear()
      responseQueue = Queue.empty
    }
  }
}
