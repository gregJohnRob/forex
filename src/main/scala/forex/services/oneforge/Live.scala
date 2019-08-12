package forex.services.oneforge

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem ⇒ TypedActorSystem }
import akka.actor.{ ActorSystem, Scheduler }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import forex.domain._
import forex.services.oneforge.Cache.RateResponse
import forex.services.oneforge.Error.ApiCallError
import forex.services.oneforge.responses.Quote
import io.circe.Json
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

final class Live[R] private[oneforge] (apiKey: String)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  import Live._

  implicit lazy val system: ActorSystem = ActorSystem()
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit lazy val timeout: Timeout = 10.seconds
  lazy val cacheSystem: TypedActorSystem[Cache.CacheMessage] = TypedActorSystem(Cache(Map()), "OneforgeCache")
  implicit lazy val scheduler: Scheduler = cacheSystem.scheduler

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val apiCall: Task[Either[Error, Rate]] = Task.defer {
      val cacheResponse = cacheSystem ? ((ref: ActorRef[Cache.RateResponse]) ⇒ Cache.GetRate(pair, ref))
      Task.fromFuture(cacheResponse).flatMap {
        case RateResponse(Some(rate)) ⇒
          Task.now(Right(rate))
        case RateResponse(None) ⇒
          callApi(pair)
            .map(_.map { rate ⇒
              cacheSystem ! Cache.PutRate(rate)
              rate
            })
      }
    }
    for {
      result ← fromTask(apiCall)
    } yield result
  }

  private def callApi(pair: Rate.Pair): Task[Either[Error, Rate]] =
    Task.defer {
      val future = Http()
        .singleRequest(HttpRequest(uri = quoteCall(pair)))
        .flatMap(Unmarshal(_).to[Json])
        .map(jsonToApiResponse)
      Task.fromFuture(future)
    }

  private def quoteCall(pair: Rate.Pair) =
    s"$URL$QUOTES${pair.from}${pair.to}&$API_KEY$apiKey"

  private def jsonToApiResponse(json: Json): Either[Error, Rate] =
    json.as[List[Quote]] match {
      case Right(value) ⇒
        Right(value.head.toRate)
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
