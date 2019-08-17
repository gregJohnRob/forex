package forex.services.oneforge

import forex.config.OneForgeConfig
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

object Interpreters {

  def live[R](oneForgeConfig: OneForgeConfig)(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] =
    new LiveInterpreter[R](new AkkaServiceCaller(oneForgeConfig), ConcurrentMapCache(oneForgeConfig.ttl))
}
