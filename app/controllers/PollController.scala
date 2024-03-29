/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ResponseConsumer

class PollController @Inject()(events: ResponseConsumer,
                               mcc: MessagesControllerComponents) extends BackendController(mcc) {

  def poll(): Action[AnyContent] = Action {
    Ok(Json.toJson(events.retrieveResponses()))
  }

  def lookup(reference: String): Action[AnyContent] = Action {
    events.lookupResponseForReference(reference).map(Ok(_)).getOrElse(NotFound)
  }

  def clear(): Action[AnyContent] = Action {
    events.clear()
    SeeOther(controllers.routes.PollController.poll.url)
  }
}
