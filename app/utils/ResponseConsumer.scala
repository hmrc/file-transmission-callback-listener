package utils

import model.{ListenerResponseSuccessfulUpload, QuarantinedFile, ResponseLog}
import org.joda.time.DateTime
import javax.inject.Singleton

trait ResponseConsumer {
  def addResponse(response: ListenerResponseSuccessfulUpload, currentDate: DateTime): Unit

  def addResponse(response: QuarantinedFile, currentDate: DateTime): Unit

  def retrieveResponses: ResponseLog
}

@Singleton
class InMemoryResponseConsumer(private val initialSuccessResponses: List[ListenerResponseSuccessfulUpload],
                               private val initialQuarantineResponses: List[QuarantinedFile],
                               private val initialDate: DateTime) extends ResponseConsumer {
  private var responsesDate: DateTime = initialDate
  private var successfulResponses: List[ListenerResponseSuccessfulUpload] = initialSuccessResponses
  private var quarantineResponses: List[QuarantinedFile] = initialQuarantineResponses

  override def addResponse(response: ListenerResponseSuccessfulUpload, currentDate: DateTime): Unit = {
    synchronized {
      checkAndRefreshCache(currentDate)
      successfulResponses = successfulResponses :+ response
    }
  }

  override def addResponse(response: QuarantinedFile, currentDate: DateTime): Unit = {
    synchronized {
      checkAndRefreshCache(currentDate)
      quarantineResponses = quarantineResponses :+ response
    }
  }

  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, successfulResponses, quarantineResponses)

  private def checkAndRefreshCache(currentDate: DateTime): Unit = {
    if (currentDate.dayOfYear().get() > responsesDate.dayOfYear().get()) {
      responsesDate = currentDate
      successfulResponses = List.empty
      quarantineResponses = List.empty
    }
  }
}