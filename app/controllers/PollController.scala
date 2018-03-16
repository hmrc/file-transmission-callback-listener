package controllers

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.ResponseConsumer

class PollController @Inject()(events: ResponseConsumer) extends Controller {
  def poll() = Action { implicit request =>
    Ok(Json.toJson(events.retrieveResponses))
  }
}
