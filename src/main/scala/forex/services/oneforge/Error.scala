package forex.services.oneforge

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

import scala.util.control.NoStackTrace

sealed trait Error extends Throwable with NoStackTrace
object Error {
  final case object Generic extends Error
  final case class System(underlying: Throwable) extends Error
  final case class ApiCallError(error: Boolean, message: String) extends Error

  object ApiCallError {
    implicit val decoder: Decoder[ApiCallError] = deriveDecoder[ApiCallError]
  }
}
