package test

import akka.actor.Actor
import akka.event.Logging

import scala.collection.mutable

/**
  * @author pblinov
  * @since 07/08/2016
  */
class TestDb extends Actor {
  val map = new mutable.HashMap[String, Object]
  val log = Logging(context.system, this)

  override def receive: Receive = {
    case SetRequest(key, value) => {
      log.info("key={}, value={}", key, value)
      map.put(key, value)
    }

    case o => {
      log.warning("Unknown: {}", o)
    }
  }
}
