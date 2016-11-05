package io.tardieu.netwemo

import akka.actor.ActorSystem
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object Netwemo {

  private val logger = LoggerFactory.getLogger(getClass)
  implicit val actorSystem = ActorSystem()

  private val checkScheduler = new CheckScheduler()(actorSystem)

  def main(args: Array[String]): Unit = {

    logger.info("Starting Netwemo")
    sys.addShutdownHook(shutdown())
    checkScheduler.scheduleOrReplaceTemperature(0.second)
    checkScheduler.scheduleOrReplaceHumidity(15.seconds)


  }

  def shutdown(): Unit = {
    logger.info("Stopping Netwemo")
    actorSystem.terminate() // TODO: Close all connection pools from the Akka Http() objects
  }


}
