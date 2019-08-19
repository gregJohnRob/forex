package forex.interfaces.api.rates

import akka.http.scaladsl._
import forex.config._
import forex.interfaces.api.utils._
import forex.main._
import forex.processes.rates.messages
import monix.eval.Task
import org.zalando.grafter.macros._

@readerOf[ApplicationConfig]
case class Routes(
    processes: Processes,
    runners: Runners
) {
  import ApiMarshallers._
  import Converters._
  import Directives._
  import processes._
  import runners._
  import server.Directives._

  lazy val route: server.Route =
    get {
      getApiRequest { req ⇒
        complete {
          val getReq = toGetRequest(req)
          if (getReq.from == getReq.to) {
            Task.now(Left(messages.Error.Api(error = true, "Pairs must be made up of two different currencies")))
          } else {
            runApp(
              Rates
                .get(getReq)
                .map(_.map(result ⇒ toGetApiResponse(result)))
            )
          }

        }
      }
    }

}
