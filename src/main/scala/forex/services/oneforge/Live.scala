package forex.services.oneforge

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import forex.domain._
import io.circe.Json
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContextExecutor, Future }

final class Live[R] private[oneforge] (apiKey: String)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  import Live._

  def quoteCall(pair: Rate.Pair) =
    s"$URL$QUOTES${pair.from}${pair.to}&$API_KEY$apiKey"

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds
  val cache = Cache(Map())

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val apiCall = Task.defer {
      val future: Future[Unit] = Http()
        .singleRequest(HttpRequest(uri = quoteCall(pair)))
        .flatMap(Unmarshal(_).to[Json])
        .map(println(_))
      Task.fromFuture(future)
    }
    for {
      result1 ← fromTask(apiCall)
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
  }
}

private[oneforge] object Live {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?pairs="
  val API_KEY = "api_key="
}
