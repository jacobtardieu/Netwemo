package io.tardieu.netwemo

import akka.actor.{ActorSystem, Cancellable}
import io.tardieu.netwemo.checkers.{HumidityChecker, RunCheck, TemperatureChecker}
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
  * Class responsible for scheduling the checks and providing method to replace them
  * @param actorSystem
  */
class CheckScheduler(implicit actorSystem: ActorSystem) {

  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] implicit val executionContext = actorSystem.dispatcher

  private[this] val netatmoConnector = new NetatmoConnector
  private[this] val wemoConnector = new WemoConnector

  private[this] val checkInterval = 10.minutes // TODO: Read from conf

  private[this] val temperatureChecker = actorSystem.actorOf(TemperatureChecker.props(wemoConnector, netatmoConnector))
  private[this] val humidityChecker = actorSystem.actorOf(HumidityChecker.props(wemoConnector, netatmoConnector))

  private[this] var tempSchedule: Option[Cancellable] = None
  private[this] var humiditySchedule: Option[Cancellable] = None

  def scheduleOrReplaceTemperature(initialDelay: FiniteDuration) = tempSchedule match {
    case Some(schedule) =>
      logger.debug("Temperature schedule already present, replacing")
      schedule.cancel()
      tempSchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, temperatureChecker, RunCheck))
    case None =>
      tempSchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, temperatureChecker, RunCheck))
  }

  def cancelTemperatureSchedule() = tempSchedule match {
    case Some(schedule) =>
      schedule.cancel()
      tempSchedule = None
    case None => logger.debug("Temperature schedule already inactive")
  }

  def scheduleOrReplaceHumidity(initialDelay: FiniteDuration) = humiditySchedule match {
    case Some(schedule) =>
      logger.debug("Humidity schedule already present, replacing")
      schedule.cancel()
      humiditySchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, humidityChecker, RunCheck))
    case None =>
      humiditySchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, humidityChecker, RunCheck))
  }

  def cancelHumiditySchedule() = humiditySchedule match {
    case Some(schedule) =>
      schedule.cancel()
      humiditySchedule = None
    case None => logger.debug("Humidity schedule already inactive")
  }

}
