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

import scala.concurrent.ExecutionContextExecutor

class AkkaServiceCaller(oneForgeConfig: OneForgeConfig) extends ServiceCaller {

  import AkkaServiceCaller._

  implicit lazy val system: ActorSystem = ActorSystem(oneForgeConfig.name)
  implicit lazy val materializer: ActorMaterializer = ActorMaterializer()
  implicit lazy val executionContext: ExecutionContextExecutor = system.dispatcher
  lazy val uri = s"$URL$QUOTES$API_KEY${oneForgeConfig.key}"

  override def getAllRates(): Task[Either[Error, List[Rate]]] =
    Task.defer {
      val future = Http()
        .singleRequest(HttpRequest(uri = uri))
        .flatMap(Unmarshal(_).to[Json])
        .map(jsonToApiResponse)
      Task.fromFuture(future)
    }

  def jsonToApiResponse(json: Json): Either[Error, List[Rate]] =
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

private[oneforge] object AkkaServiceCaller {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?"
  val API_KEY = "api_key="
}
