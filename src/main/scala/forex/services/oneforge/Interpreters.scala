package forex.services.oneforge

import forex.config.OneForgeConfig
import forex.domain.Rate
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {

  def live[R](oneForgeConfig: OneForgeConfig)(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] =
    new Live[R](new AkkaServiceCaller(oneForgeConfig), ConcurrentMapCache(oneForgeConfig.ttl))
}

class Live[R] private[oneforge] (serviceCaller: ServiceCaller, val cache: Cache)(
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {

  override def get(pair: Rate.Pair): Eff[R, Either[Error, Rate]] = {
    val apiCall: Task[Either[Error, Rate]] = Task.defer {
      cache.get(pair) match {
        case Some(value) ⇒
          Task.now(Right(value))
        case None ⇒
          serviceCaller
            .getAllRates()
            .map(
              x ⇒
                x.flatMap { rates ⇒
                  cache.putAll(rates)
                  cache.get(pair) match {
                    case Some(value) ⇒
                      Right(value)
                    case None ⇒
                      Left(Error.System(new Exception("Could not retrieve pair in cache")))
                  }
              }
            )
      }
    }
    for {
      result ← fromTask(apiCall)
    } yield result
  }
}
