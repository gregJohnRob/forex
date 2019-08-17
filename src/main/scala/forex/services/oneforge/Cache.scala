package forex.services.oneforge

import forex.domain.Rate

trait Cache {
  def get(pair: Rate.Pair): Option[Rate]
  def put(rate: Rate): Option[Rate]
  def putAll(rates: Seq[Rate]): Unit
}
