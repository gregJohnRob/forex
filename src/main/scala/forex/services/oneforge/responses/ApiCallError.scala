package forex.services.oneforge.responses

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder
import forex.services.oneforge.Error

case class ApiCallError(error: Boolean, message: String) {
  def toError = {
    Error.System(new Exception(message))
  }
}

object ApiCallError {
  implicit val decoder: Decoder[ApiCallError] = deriveDecoder[ApiCallError]
}