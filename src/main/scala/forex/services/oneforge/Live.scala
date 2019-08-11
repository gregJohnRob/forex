package forex.services.oneforge

import java.time.OffsetDateTime

import akka.actor.{ ActorSystem, Scheduler }
import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ ActorRef, ActorSystem ⇒ TypedActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.Timeout
import forex.domain.{ Rate, _ }
import forex.services.oneforge.Cache.RateResponse
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
  val dummyPair = Rate.Pair(Currency.GBP, Currency.USD)
  val dummyRate = Rate(dummyPair, Price(BigDecimal(100)), Timestamp(OffsetDateTime.now.minusMinutes(6)))
  val validDummyRate = Rate(dummyPair, Price(BigDecimal(10)), Timestamp(OffsetDateTime.now))
  val cacheSystem: TypedActorSystem[Cache.CacheMessage] =
    TypedActorSystem(Cache(Map(dummyPair → dummyRate)), "OneforgeCache")
  val cacheRef: ActorRef[Cache.CacheMessage] = cacheSystem
  implicit val scheduler: Scheduler = cacheSystem.scheduler

  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] = {
    val apiCall = Task.defer {
      val cacheResponse = cacheSystem ? ((ref: ActorRef[Cache.RateResponse]) ⇒ Cache.GetRate(pair, ref))
      val future = cacheResponse
        .map {
          case RateResponse(Some(rate)) ⇒ // Something was in the cache that is still valid, so return
            rate
          case RateResponse(None) ⇒ // Did not see something valid in cache, query the api and update the cache
            cacheSystem ! Cache.PutRate(validDummyRate)
            validDummyRate
        }
//        Http()
//        .singleRequest(HttpRequest(uri = quoteCall(pair)))
//        .flatMap(Unmarshal(_).to[Json])
//        .map(println(_))
      Task.fromFuture(future)
    }
    for {
      result ← fromTask(apiCall)
//      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
  }
}

private[oneforge] object Live {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?pairs="
  val API_KEY = "api_key="
}
