package forex.processes.rates

import forex.domain._
import scala.util.control.NoStackTrace

package messages {

  import io.circe._
  import io.circe.generic.semiauto._

  sealed trait Error extends Throwable with NoStackTrace
  object Error {
    final case object Generic extends Error
    final case class System(underlying: Throwable) extends Error
    final case class Api(error: Boolean, message: String) extends Error

    object Api {
      implicit val encoder: Encoder[Api] = deriveEncoder[Api]
    }
  }

  final case class GetRequest(
      from: Currency,
      to: Currency
  )
}
