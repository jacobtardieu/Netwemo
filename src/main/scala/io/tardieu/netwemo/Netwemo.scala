package io.tardieu.netwemo

import akka.actor.ActorSystem
import io.tardieu.netwemo.checkers.{HumidityChecker, RunCheck, TemperatureChecker}
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Netwemo {

  private val logger = LoggerFactory.getLogger(getClass)
  implicit val actorSystem = ActorSystem()

  def main(args: Array[String]): Unit = {

    logger.info("Starting Netwemo")

    val netatmoConnector = new NetatmoConnector
    val wemoConnector = new WemoConnector

    sys.addShutdownHook(shutdown())

    val temperatureChecker = actorSystem.actorOf(TemperatureChecker.props(wemoConnector, netatmoConnector))
    val humidityChecker = actorSystem.actorOf(HumidityChecker.props(wemoConnector, netatmoConnector))

    actorSystem.scheduler.schedule(0.second, 1.minute, temperatureChecker, RunCheck)
    actorSystem.scheduler.schedule(5.second, 1.minute, humidityChecker, RunCheck)

  }

  def shutdown(): Unit = {
    logger.info("Stopping Netwemo")
    actorSystem.terminate() // TODO: Close all connection pools from the Akka Http() objects
  }


}
