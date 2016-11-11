package io.tardieu.netwemo

import java.util.concurrent.TimeUnit

import akka.actor.{ActorRef, ActorSystem, Cancellable}
import com.typesafe.config.ConfigFactory
import io.tardieu.netwemo.checkers.RunCheck
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

/**
  * Class responsible for scheduling the checks and providing method to replace them
  * @param actorSystem
  */
class CheckScheduler(wemoConnector: WemoConnector,
                     netatmoConnector: NetatmoConnector,
                     temperatureChecker: ActorRef,
                     humidityChecker: ActorRef
                    )(implicit actorSystem: ActorSystem) {

  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] implicit val executionContext = actorSystem.dispatcher

  private[this] val conf = ConfigFactory.load().getConfig("scheduler")

  private[this] val checkInterval =
    FiniteDuration(conf.getDuration("checkInterval").getNano, TimeUnit.NANOSECONDS)

  private[netwemo] var tempSchedule: Option[Cancellable] = None
  private[netwemo] var humiditySchedule: Option[Cancellable] = None

  def scheduleOrReplaceTemperature(initialDelay: FiniteDuration) = {
    tempSchedule.foreach { schedule =>
      logger.debug("Temperature schedule already present, replacing")
      schedule.cancel()
    }
    tempSchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, temperatureChecker, RunCheck))
  }

  def cancelTemperatureSchedule() = {
    tempSchedule.fold(logger.debug("Temperature schedule already inactive"))(_.cancel())
    tempSchedule = None
  }

  def scheduleOrReplaceHumidity(initialDelay: FiniteDuration) = {
    humiditySchedule.foreach { schedule =>
      logger.debug("Humidity schedule already present, replacing")
      schedule.cancel()
    }
    humiditySchedule = Some(actorSystem.scheduler.schedule(initialDelay, checkInterval, humidityChecker, RunCheck))
  }

  def cancelHumiditySchedule() = {
    humiditySchedule.fold(logger.debug("Humidity schedule already inactive"))(_.cancel())
    humiditySchedule = None
  }

}
