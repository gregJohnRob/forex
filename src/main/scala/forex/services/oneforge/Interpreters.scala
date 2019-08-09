package forex.services.oneforge

import java.time.OffsetDateTime

import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.all._
import org.atnos.eff.addon.monix.task._

object Interpreters {
  def live[R](api_key: String)(
      implicit
      m1: _task[R]
  ): Algebra[Eff[R, ?]] = new Live[R](api_key)
}

final class Dummy[R] private[oneforge] (
    implicit
    m1: _task[R]
) extends Algebra[Eff[R, ?]] {
  override def get(
      pair: Rate.Pair
  ): Eff[R, Error Either Rate] =
    for {
      result ← fromTask(Task.now(Rate(pair, Price(BigDecimal(100)), Timestamp.now)))
    } yield Right(result)
}
