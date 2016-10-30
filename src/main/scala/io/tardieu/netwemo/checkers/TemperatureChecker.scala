package io.tardieu.netwemo.checkers

import java.time.{DayOfWeek, LocalDateTime, LocalTime}

import akka.actor.Props
import com.typesafe.config.ConfigFactory
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}

import scala.concurrent.Future

object TemperatureChecker {

  def props(wemoConnector: WemoConnector, netatmoConnector: NetatmoConnector): Props =
    Props(new TemperatureChecker(wemoConnector, netatmoConnector))

}

class TemperatureChecker private (val wemoConnector: WemoConnector, val netatmoConnector: NetatmoConnector)
  extends Checker {

  val conf = ConfigFactory.load.getConfig("temperature")

  // Those values should be read from the database
  val lowThreshold = conf.getDouble("lowThreshold").toFloat
  val highThreshold = conf.getDouble("highThreshold").toFloat
  val coldLowThreshold = conf.getDouble("coldLowThreshold").toFloat
  val coldHighThreshold = conf.getDouble("coldHighThreshold").toFloat
  val coldStartTime: LocalTime = LocalTime.of(conf.getInt("coldHourStart"), conf.getInt("coldMinuteStart"))
  val coldStopTime: LocalTime = LocalTime.of(conf.getInt("coldHourStop"), conf.getInt("coldMinuteStop"))

  override val deviceName = conf.getString("deviceName")

  private def isWorkingDay: Boolean =
    LocalDateTime.now.getDayOfWeek match {
      case DayOfWeek.SATURDAY | DayOfWeek.SUNDAY => false
      case _ => true
    }

  private def inColdHours(startTime: LocalTime, stopTime: LocalTime): Boolean = {
    val now = LocalTime.now()
    val b = now.isAfter(startTime) && now.isBefore(stopTime) && isWorkingDay
    log.debug("In cold hours: {}", b)
    b
  }

  override def checkValue: Future[Float] = netatmoConnector.getTemperature

  override def computeDesiredState(value: Float): Option[Boolean] = {
    inColdHours(coldStartTime, coldStopTime) match {
      case true => computeState(value, coldLowThreshold, coldHighThreshold)
      case false => computeState(value, lowThreshold, highThreshold)
    }
  }

}
