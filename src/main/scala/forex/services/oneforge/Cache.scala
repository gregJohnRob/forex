package forex.services.oneforge

import java.time.OffsetDateTime

import akka.actor.typed.{ ActorRef, Behavior }
import akka.actor.typed.scaladsl.{ AbstractBehavior, ActorContext, Behaviors }
import forex.domain.Rate
import forex.services.oneforge.Cache.{ GetRate, PutRate, RateResponse }

object Cache {

  def apply(cache: Map[Rate.Pair, Rate]): Behavior[CacheMessage] =
    Behaviors.setup(context ⇒ new Cache(context, cache))

  sealed trait CacheMessage
  final case class GetRate(pair: Rate.Pair, replyTo: ActorRef[RateResponse]) extends CacheMessage
  final case class RateResponse(rateMaybe: Option[Rate]) extends CacheMessage
  final case class PutRate(rate: Rate) extends CacheMessage
}

class Cache(context: ActorContext[Cache.CacheMessage], var cache: Map[Rate.Pair, Rate])
    extends AbstractBehavior[Cache.CacheMessage] {
  override def onMessage(msg: Cache.CacheMessage): Behavior[Cache.CacheMessage] =
    msg match {
      case GetRate(pair, replyTo) ⇒
        val rateMaybe = cache.get(pair)
        if (rateMaybe.isEmpty) {
          replyTo ! RateResponse(None)
        } else if (rateMaybe.get.timestamp.value.isBefore(OffsetDateTime.now.minusMinutes(5))) {
          cache = cache - pair
          replyTo ! RateResponse(None)
        } else {
          replyTo ! RateResponse(rateMaybe)
        }
        this
      case PutRate(rate) ⇒
        cache = cache + (rate.pair → rate)
        this
    }
}
