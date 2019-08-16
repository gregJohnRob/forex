package forex.services.oneforge

import java.time.OffsetDateTime

import forex.domain.Rate

import scala.collection.concurrent.{ Map, TrieMap }

class ConcurrentMapCache(cache: Map[Rate.Pair, Rate]) {

  def get(pair: Rate.Pair): Option[Rate] =
    cache get (pair) match {
      case Some(value) ⇒
        if (value.timestamp.value.isBefore(OffsetDateTime.now.minusMinutes(5))) {
          cache.remove(pair)
          None
        } else {
          Some(value)
        }
      case None ⇒
        None
    }

  def put(rate: Rate): Option[Rate] =
    cache.put(rate.pair, rate)
}

object ConcurrentMapCache {
  def apply(): ConcurrentMapCache = new ConcurrentMapCache(new TrieMap[Rate.Pair, Rate]())
}
