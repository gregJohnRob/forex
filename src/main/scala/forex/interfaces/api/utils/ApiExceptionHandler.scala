package forex.interfaces.api.utils

import akka.http.scaladsl._
import forex.processes._

object ApiExceptionHandler {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case RatesError.System(underlying) ⇒
        ctx ⇒
          ctx.complete(underlying.getLocalizedMessage)
      case RatesError.Generic ⇒
        ctx ⇒
          ctx.complete("Something went wrong in the rates process")
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete("Something else went wrong")
    }

}
