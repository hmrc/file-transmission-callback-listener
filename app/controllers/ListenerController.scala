package controllers

import javax.inject.Inject

import connectors.FileHashRetriever
import model.{ListenerRequest, ListenerResponse}
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, AnyContent, Controller}
import utils.CallbackConsumer

import scala.concurrent.{ExecutionContext, Future}

class ListenerController @Inject()(connector: FileHashRetriever,
                                   consumer: CallbackConsumer)(implicit ec: ExecutionContext) extends Controller {

  def listen(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[ListenerRequest] match {
        case JsSuccess(listenerRequest, _) =>
          connector.fileHash(listenerRequest.downloadUrl) map { md5 =>
            val response = ListenerResponse(listenerRequest.reference, listenerRequest.downloadUrl, md5)
            consumer.logSuccessfulResponse(response)
            Ok(Json.toJson(response))
          } recover { case t: Throwable =>
            consumer.logHashError(listenerRequest, t.getMessage)
            InternalServerError(EmptyContent())
          }
        case _: JsError =>
          consumer.logInvalidJson(json)
          Future.successful(BadRequest(EmptyContent()))
      }
      case None =>
        consumer.logInvalidBody(request.body.asText.getOrElse("Could not retrieve request body"))
        Future.successful(BadRequest(EmptyContent()))
    }
  }
}

