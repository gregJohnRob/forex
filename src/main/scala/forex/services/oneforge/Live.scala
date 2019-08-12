package forex.services.oneforge

import java.time.OffsetDateTime

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem => TypedActorSystem}
import akka.actor.{ActorSystem, Scheduler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import forex.domain._
import forex.services.oneforge.Cache.RateResponse
import forex.services.oneforge.responses.{ApiCallError, Quote}
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

  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val timeout: Timeout = 10.seconds
  val dummyPair = Rate.Pair(Currency.GBP, Currency.USD)
  val invalidDummyRate = Rate(dummyPair, Price(BigDecimal(100)), Timestamp(OffsetDateTime.now.minusMinutes(6)))
  val validDummyRate = Rate(dummyPair, Price(BigDecimal(10)), Timestamp(OffsetDateTime.now))
  val cacheSystem: TypedActorSystem[Cache.CacheMessage] =
    TypedActorSystem(Cache(Map()), "OneforgeCache")
  val cacheRef: ActorRef[Cache.CacheMessage] = cacheSystem
  implicit val scheduler: Scheduler = cacheSystem.scheduler

  private def quoteCall(pair: Rate.Pair) =
    s"$URL$QUOTES${pair.from}${pair.to}&$API_KEY$apiKey"

  private def checkCache(pair: Rate.Pair): Task[RateResponse] =
    Task.defer {
      val cacheResponse = cacheSystem ? ((ref: ActorRef[Cache.RateResponse]) ⇒ Cache.GetRate(pair, ref))
      Task.fromFuture(cacheResponse)
    }

  private def callApiWithErrors(json: Json): Either[Error, Rate] =
    json.as[List[Quote]] match {
      case Right(value) ⇒
        Right(value.head.toRate)
      case Left(_) ⇒
        json.as[ApiCallError] match {
          case Right(error) ⇒ Left(error.toError)
          case Left(_) ⇒ Left(Error.Generic)
        }
    }

  private def callApi(pair: Rate.Pair): Task[Either[Error, Rate]] =
    Task.defer {
      val future = Http()
        .singleRequest(HttpRequest(uri = quoteCall(pair)))
        .flatMap(Unmarshal(_).to[Json])
        .map(callApiWithErrors)
      Task.fromFuture(future)
    }

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val apiCall: Task[Either[Error, Rate]] = Task.defer {
      val cacheResponse = cacheSystem ? ((ref: ActorRef[Cache.RateResponse]) ⇒ Cache.GetRate(pair, ref))
      Task.fromFuture(cacheResponse).flatMap {
        case RateResponse(Some(rate)) ⇒
          Task.now(Right(rate))
        case RateResponse(None) ⇒
          val call = callApi(pair)
          call.map {
            case Right(value) ⇒
              cacheSystem ! Cache.PutRate(value)
              Right(value)
            case Left(value) ⇒
              Left(value)
          }
      }
    }
    for {
      result ← fromTask(apiCall)
    } yield result
  }
}

private[oneforge] object Live {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?pairs="
  val API_KEY = "api_key="
}
