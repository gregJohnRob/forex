package forex.services.oneforge

import java.time.OffsetDateTime

import forex.domain.Rate

import scala.collection.concurrent.{ Map, TrieMap }

class ConcurrentMapCache(cache: Map[Rate.Pair, Rate], ttl: Long) extends Cache {

  def get(pair: Rate.Pair): Option[Rate] = this.synchronized {
    cache get (pair) match {
      case Some(value) ⇒
        if (value.timestamp.value.isBefore(OffsetDateTime.now.minusMinutes(ttl))) {
          cache.remove(pair)
          None
        } else {
          Some(value)
        }
      case None ⇒
        None
    }
  }

  def put(rate: Rate): Option[Rate] = this.synchronized {
    cache.put(rate.pair, rate)
  }

  def putAll(rates: Seq[Rate]): Unit = this.synchronized {
    rates.foreach(rate ⇒ cache.put(rate.pair, rate))
  }
}

object ConcurrentMapCache {
  def apply(ttl: Long): ConcurrentMapCache = new ConcurrentMapCache(new TrieMap[Rate.Pair, Rate](), ttl)
}
