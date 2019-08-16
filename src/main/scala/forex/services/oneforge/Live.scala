package forex.services.oneforge

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import forex.config.OneForgeConfig
import forex.domain._
import forex.services.oneforge.Error.ApiCallError
import forex.services.oneforge.responses.Quote
import io.circe.Json
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

import scala.concurrent.ExecutionContextExecutor

final class Live[R] private[oneforge] (oneForgeConfig: OneForgeConfig)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  import Live._

  implicit lazy val system: ActorSystem = ActorSystem(oneForgeConfig.name)
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = system.dispatcher
  val cache: ConcurrentMapCache = ConcurrentMapCache(oneForgeConfig.ttl)

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val apiCall: Task[Either[Error, Rate]] = Task.defer {
      cache.get(pair) match {
        case Some(value) ⇒
          Task.now(Right(value))
        case None ⇒
          callApi(pair)
            .map(x => x.flatMap { rates ⇒
              cache.putAll(rates)
              cache.get(pair) match {
                case Some(value) =>
                  Right(value)
                case None =>
                  Left(Error.Generic)
              }
            })
      }
    }
    for {
      result ← fromTask(apiCall)
    } yield result
  }

  private def callApi(pair: Rate.Pair): Task[Either[Error, List[Rate]]] =
    Task.defer {
      val future = Http()
        .singleRequest(HttpRequest(uri = quoteCall(pair)))
        .flatMap(Unmarshal(_).to[Json])
        .map(jsonToApiResponse)
      Task.fromFuture(future)
    }

  private def quoteCall(pair: Rate.Pair) =
    s"$URL$QUOTES${pair.from}${pair.to}&$API_KEY${oneForgeConfig.key}"

  private def jsonToApiResponse(json: Json): Either[Error, List[Rate]] =
    json.as[List[Quote]] match {
      case Right(value) ⇒
        Right(value.map(_.toRate))
      case Left(_) ⇒
        json.as[ApiCallError] match {
          case Right(error) ⇒ Left(error)
          case Left(_)      ⇒ Left(Error.Generic)
        }
    }
}

private[oneforge] object Live {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?pairs="
  val API_KEY = "api_key="
}
