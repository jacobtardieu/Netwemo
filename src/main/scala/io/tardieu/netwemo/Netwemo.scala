package io.tardieu.netwemo

import akka.actor.ActorSystem
import io.tardieu.netwemo.checkers.{HumidityChecker, TemperatureChecker}
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

object Netwemo {

  private val logger = LoggerFactory.getLogger(getClass)
  implicit val actorSystem = ActorSystem()

  private val wemoConnector = new WemoConnector()
  private val netatmoConnector = new NetatmoConnector()

  private val temperatureChecker = actorSystem.actorOf(TemperatureChecker.props(wemoConnector, netatmoConnector))
  private val humidityChecker = actorSystem.actorOf(HumidityChecker.props(wemoConnector, netatmoConnector))


  private val checkScheduler = new CheckScheduler(
    wemoConnector, netatmoConnector,
    temperatureChecker, humidityChecker
  )
  private val server = new Server(checkScheduler)

  def main(args: Array[String]): Unit = {

    logger.info("Starting Netwemo")
    sys.addShutdownHook(shutdown())
    checkScheduler.scheduleOrReplaceTemperature(0.second)
    checkScheduler.scheduleOrReplaceHumidity(15.seconds)

  }

  def shutdown(): Unit = {
    logger.info("Stopping Netwemo")
    server.stopServer()
    actorSystem.terminate() // TODO: Close all connection pools from the Akka Http() objects
  }


}
