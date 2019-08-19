package forex.services.oneforge

import java.time.{ Instant, OffsetDateTime, ZoneId }

import forex.config.OneForgeConfig
import forex.domain.{ Currency, Price, Rate, Timestamp }
import io.circe.parser._
import org.scalatest.{ Matchers, WordSpec }

class AkkaServiceCallerSpec extends WordSpec with Matchers {

  val config = OneForgeConfig("", "", 5)
  val serviceCaller = new AkkaServiceCaller(config)

  "AkkaServiceCaller" should {
    "return the error forex returns" in {
      val message =
        """
          |{
          |    "error": true,
          |    "message": "API Key Not Valid. Please go to 1forge.com to get an API key. If you have any questions please email us at contact@1forge.com"
          |}
          |""".stripMargin
      val actual = parse(message).map(serviceCaller.jsonToApiResponse)
      val expected = Right(
        Left(
          Error.ApiCallError(
            error = true,
            "API Key Not Valid. Please go to 1forge.com to get an API key. If you have any questions please email us at contact@1forge.com"
          )
        )
      )
      actual should be(expected)
    }

    "return a list of rates" in {
      val message =
        """
          |[
          | {
          |   "symbol": "EURUSD",
          |   "bid": 1.10893,
          |   "ask": 1.10921,
          |   "price": 1.10907,
          |   "quota_used": 50975,
          |   "timestamp": 1566125600
          |  }
          |]
          |""".stripMargin
      val actual = parse(message).map(serviceCaller.jsonToApiResponse)
      val expected = Right(
        Right(
          List(
            Rate(
              Rate.Pair(Currency.EUR, Currency.USD),
              Price(1.10907),
              Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(1566125600L * 1000), ZoneId.of("UTC")))
            )
          )
        )
      )
      actual should be(expected)
    }

    "only return rates for known currencies" in {
      val message =
        """
          |[
          | {
          |   "symbol": "EURUSD",
          |   "bid": 1.10893,
          |   "ask": 1.10921,
          |   "price": 1.10907,
          |   "quota_used": 50975,
          |   "timestamp": 1566125600
          |  },
          |  {
          |   "symbol": "ABCEFG",
          |   "bid": 1.10893,
          |   "ask": 1.10921,
          |   "price": 1.10907,
          |   "quota_used": 50975,
          |   "timestamp": 1566125600
          |  }
          |]
          |""".stripMargin
      val actual = parse(message).map(serviceCaller.jsonToApiResponse)
      val expected = Right(
        Right(
          List(
            Rate(
              Rate.Pair(Currency.EUR, Currency.USD),
              Price(1.10907),
              Timestamp(OffsetDateTime.ofInstant(Instant.ofEpochMilli(1566125600L * 1000), ZoneId.of("UTC")))
            )
          )
        )
      )
      actual should be(expected)
    }
  }

}
