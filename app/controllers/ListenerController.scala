package controllers

import javax.inject.Inject

import connectors.FileHashRetriever
import model.{ListenerResponseSuccessfulUpload, QuarantinedFile, UploadedFile}
import org.joda.time.DateTime
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, AnyContent, Controller, Result}
import utils.{CallbackConsumer, ResponseConsumer}

import scala.concurrent.{ExecutionContext, Future}

class ListenerController @Inject()(connector: FileHashRetriever,
                                   callbackConsumer: CallbackConsumer,
                                   responseConsumer: ResponseConsumer
                                  )(implicit ec: ExecutionContext) extends Controller {

  def listen(): Action[AnyContent] = Action.async { implicit request =>
    request.body.asJson match {
      case Some(json) => json.validate[UploadedFile] match {
        case JsSuccess(uploadedFile, _) => handleSuccessfulUploadCallback(uploadedFile)
        case _: JsError =>
          json.validate[QuarantinedFile] match {
            case JsSuccess(quarantinedFile, _) => handleQuarantineUploadCallback(quarantinedFile)
            case _ =>
              callbackConsumer.logInvalidJson(json)
              Future.successful(BadRequest(EmptyContent()))
          }
      }
      case None =>
        callbackConsumer.logInvalidBody(request.body.asText.getOrElse("Could not retrieve request body"))
        Future.successful(BadRequest(EmptyContent()))
    }
  }

  private def handleSuccessfulUploadCallback(uploadedFile: UploadedFile): Future[Result] = {
    connector.fileHash(uploadedFile.downloadUrl) map { md5 =>
      val response = ListenerResponseSuccessfulUpload(uploadedFile.reference, uploadedFile.downloadUrl, md5)
      callbackConsumer.logSuccessfulResponse(response)
      responseConsumer.addResponse(response, DateTime.now())
      Ok(Json.toJson(response))
    } recover { case t: Throwable =>
      callbackConsumer.logHashError(uploadedFile, t.getMessage)
      InternalServerError(EmptyContent())
    }
  }

  private def handleQuarantineUploadCallback(quarantinedFile: QuarantinedFile): Future[Result] = {
    callbackConsumer.logQuarantinedResponse(quarantinedFile)
    responseConsumer.addResponse(quarantinedFile, DateTime.now())
    Future.successful(Ok(Json.toJson(quarantinedFile)))
  }
}
