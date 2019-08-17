package forex.services.oneforge

import java.time.OffsetDateTime

import forex.domain.{Currency, Price, Rate, Timestamp}
import forex.main.AppStack
import monix.eval.Task
import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

import scala.collection.concurrent.TrieMap

class LiveSpec extends WordSpec with Matchers with MockFactory {

  val pair = new Rate.Pair(Currency.GBP, Currency.USD)
  val secondPair = new Rate.Pair(Currency.GBP, Currency.CAD)
  val ttl = 5

  "Live" should {
    "return the rate in the cache if it is valid" in {
      val rate = new Rate(pair, new Price(10), Timestamp.now)
      val cache: ConcurrentMapCache = new ConcurrentMapCache(new TrieMap[Rate.Pair, Rate](), ttl)
      cache.put(rate)
      val serviceCaller = new ServiceCaller {
        override def getAllRates(): Task[Either[Error, List[Rate]]] = ???
      }
      val liveInterpreter = new LiveInterpreter[AppStack](serviceCaller, cache)
      for {
        x ← liveInterpreter.get(pair)
      } yield x should be(Right(rate))
    }

    "attempt to refresh the cache if it contains a rate that is no longer valid" in {
      val invalidOffset = OffsetDateTime.now.minusMinutes(6)
      val invalidRate = new Rate(pair, new Price(10), new Timestamp(invalidOffset))
      val validRate = new Rate(pair, new Price(100), Timestamp.now)
      val cache: ConcurrentMapCache = new ConcurrentMapCache(new TrieMap[Rate.Pair, Rate](), ttl)
      cache.put(invalidRate)
      val serviceCaller = new ServiceCaller {
        override def getAllRates(): Task[Either[Error, List[Rate]]] = Task.now(Right(List(validRate)))
      }
      val liveInterpreter = new LiveInterpreter[AppStack](serviceCaller, cache)
      for {
        x ← liveInterpreter.get(pair)
      } yield x should be(Right(validRate))
    }

  }

}
