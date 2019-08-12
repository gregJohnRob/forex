package forex.services.oneforge

import java.time.{ Instant, OffsetDateTime, ZoneId }

import forex.domain.{ Currency, Price, Rate, Timestamp }
import io.circe._
import io.circe.generic.semiauto._

case class OneForgeResponse(symbol: String, bid: BigDecimal, ask: BigDecimal, price: BigDecimal, timestamp: Long) {
  def asRate() = {
    val pair = Rate.Pair(Currency.fromString(symbol.take(3)), Currency.fromString(symbol.drop(3)))
    Rate(
      pair,
      Price(price),
      Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp * 1000), ZoneId.systemDefault()))
    )
  }
}

object OneForgeResponse {
  implicit val decoder: Decoder[OneForgeResponse] = deriveDecoder[OneForgeResponse]
}
