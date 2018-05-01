package utils.logging

import org.slf4j.MDC
import play.api.libs.json.JsValue

import scala.PartialFunction.condOpt

object WithFileReference {
  def withFileReferenceLogged[T](response: JsValue)(block: => T): T = {
    try {
      condOpt((response \ "reference").asOpt[String]) {
        case Some(fileReference) => MDC.put("file-reference", fileReference)
      }

      block
    } finally {
      MDC.remove("file-reference")
    }
  }
}
