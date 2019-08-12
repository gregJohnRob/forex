package forex.interfaces.api.utils

import akka.http.scaladsl._
import forex.processes._
import io.circe.syntax._

object ApiExceptionHandler {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case RatesError.Generic ⇒
        ctx ⇒
          ctx.complete("Something went wrong in the rates process")
      case RatesError.System(underlying) ⇒
          ctx ⇒
          ctx.complete(underlying.getLocalizedMessage)
      case err: RatesError.Api ⇒
          ctx ⇒
          ctx.complete(err.asJson.toString())
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete("Something else went wrong")
    }

}
