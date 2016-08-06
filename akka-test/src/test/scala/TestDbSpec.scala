import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import org.scalatest.{BeforeAndAfterEach, FunSpecLike, Matchers}
import test.{SetRequest, TestDb}

/**
  * @author pblinov
  * @since 07/08/2016
  */
class TestDbSpec extends FunSpecLike with Matchers with BeforeAndAfterEach {
  implicit val system = ActorSystem()
  describe("TestDB") {
    describe("given SetRequest") {
      it("should place key/value pair into map") {
        val actorRef = TestActorRef(new TestDb)
        actorRef ! SetRequest("key", "value")
        val testDb = actorRef.underlyingActor
        testDb.map.get("key") should equal(Some("value"))
      }
    }
  }
}
