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

import java.time.LocalDate

import javax.inject.Inject
import play.api.Logging
import play.api.mvc.Results.EmptyContent
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ResponseConsumer

class ListenerController @Inject()(responseConsumer: ResponseConsumer,
                                   mcc: MessagesControllerComponents)
    extends BackendController(mcc) with Logging {

  def listen(): Action[AnyContent] = Action { implicit request =>
    logger.debug(s"Received request with body: [${request.body.toString}].")

    request.body.asJson match {
      case Some(json) =>
        responseConsumer.addResponse(json, LocalDate.now())
        Ok(json)
      case None =>
        logger.error(s"Request body cannot be parsed as JSON, request body is: ${request.body.toString}")
        BadRequest(EmptyContent())
    }
  }

}
