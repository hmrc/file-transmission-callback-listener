package utils

import model.{ListenerResponse, ResponseLog}
import org.joda.time.DateTime
import javax.inject.Singleton

trait ResponseConsumer {
  def addResponse(response: ListenerResponse, currentDate: DateTime): Unit

  def retrieveResponses: ResponseLog
}

@Singleton
class InMemoryResponseConsumer(private val initialResponses: List[ListenerResponse],
                               private val initialDate: DateTime) extends ResponseConsumer {
  private var responsesDate: DateTime = initialDate
  private var responses: List[ListenerResponse] = initialResponses

  override def addResponse(response: ListenerResponse, currentDate: DateTime): Unit = {
    synchronized {
      if (currentDate.dayOfYear().get() > responsesDate.dayOfYear().get()) {
        responsesDate = currentDate
        responses = List(response)
      } else {
        responses = responses :+ response
      }
    }
  }

  override def retrieveResponses: ResponseLog = ResponseLog(responsesDate, responses)
}