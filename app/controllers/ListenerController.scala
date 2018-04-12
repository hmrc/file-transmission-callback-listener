package controllers

import javax.inject.Inject

import org.joda.time.DateTime
import play.api.Logger
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, AnyContent, Controller}
import utils.ResponseConsumer

import scala.concurrent.ExecutionContext

class ListenerController @Inject()(responseConsumer: ResponseConsumer)(implicit ec: ExecutionContext) extends Controller {

  def listen(): Action[AnyContent] = Action { implicit request =>
    request.body.asJson match {
      case Some(json) =>
        responseConsumer.addResponse(json, DateTime.now())
        Ok(json)
      case None =>
        Logger.error(s"Request body cannot be parsed as JSON, request body is: ${request.body.toString}")
        BadRequest(EmptyContent())
    }
  }
}
