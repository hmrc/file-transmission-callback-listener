package controllers

import javax.inject.Inject

import connectors.FileHashRetriever
import model.{ListenerRequest, ListenerResponse}
import org.joda.time.DateTime
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, AnyContent, Controller}
import utils.{CallbackConsumer, ResponseConsumer}

import scala.concurrent.{ExecutionContext, Future}

class ListenerController @Inject()(connector: FileHashRetriever,
                                   callbackConsumer: CallbackConsumer,
                                   responseConsumer: ResponseConsumer
                                  )(implicit ec: ExecutionContext) extends Controller {

  def listen(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[ListenerRequest] match {
        case JsSuccess(listenerRequest, _) =>
          connector.fileHash(listenerRequest.downloadUrl) map { md5 =>
            val response = ListenerResponse(listenerRequest.reference, listenerRequest.downloadUrl, md5)
            callbackConsumer.logSuccessfulResponse(response)
            responseConsumer.addResponse(response, DateTime.now())
            Ok(Json.toJson(response))
          } recover { case t: Throwable =>
            callbackConsumer.logHashError(listenerRequest, t.getMessage)
            InternalServerError(EmptyContent())
          }
        case _: JsError =>
          callbackConsumer.logInvalidJson(json)
          Future.successful(BadRequest(EmptyContent()))
      }
      case None =>
        callbackConsumer.logInvalidBody(request.body.asText.getOrElse("Could not retrieve request body"))
        Future.successful(BadRequest(EmptyContent()))
    }
  }
}
