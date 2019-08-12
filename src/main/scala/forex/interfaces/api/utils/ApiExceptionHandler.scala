package forex.interfaces.api.utils

import akka.http.scaladsl._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import forex.processes._
import io.circe.syntax._

object ApiExceptionHandler {

  def apply(): server.ExceptionHandler =
    server.ExceptionHandler {
      case RatesError.Generic ⇒
        ctx ⇒
          ctx.complete(
            HttpResponse(
              status = StatusCodes.InternalServerError,
              entity = "Something went wrong in the rates process"
            )
          )
      case RatesError.System(underlying) ⇒
        ctx ⇒
          ctx.complete(HttpResponse(
            status = StatusCodes.InternalServerError,
            entity = underlying.getLocalizedMessage
          ))
      case err: RatesError.Api ⇒
        ctx ⇒
          ctx.complete(HttpResponse(
            status = StatusCodes.InternalServerError,
            entity = err.asJson.toString()
          ))
      case _: Throwable ⇒
        ctx ⇒
          ctx.complete(HttpResponse(
            status = StatusCodes.InternalServerError,
            entity = "Something else went wrong"
          ))
    }

}
