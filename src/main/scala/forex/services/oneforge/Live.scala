package forex.services.oneforge


import forex.domain._
import monix.eval.Task
import org.atnos.eff._
import org.atnos.eff.addon.monix.task._

final class Live[R] private[oneforge] (apiKey: String)(
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

private[oneforge] object Live {
  val URL: String = "https://forex.1forge.com/1.0.3"
  val QUOTES: String = "/quotes?pairs="
  val API_KEY = "api_key="
  def quoteCall(pair: Rate.Pair, apiKey: String) =
    s"$URL$QUOTES${pair.from}${pair.to}&$API_KEY$apiKey"

}
