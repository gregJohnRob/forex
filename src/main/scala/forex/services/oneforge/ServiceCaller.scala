package forex.services.oneforge

import forex.domain.Rate
import monix.eval.Task

trait ServiceCaller {
  def getAllRates(): Task[Either[Error, List[Rate]]]
}
