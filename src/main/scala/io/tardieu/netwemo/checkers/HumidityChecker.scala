package io.tardieu.netwemo.checkers
import java.time.LocalTime

import akka.actor.Props
import com.typesafe.config.ConfigFactory
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}

import scala.concurrent.Future

object HumidityChecker {

  def props(wemoConnector: WemoConnector, netatmoConnector: NetatmoConnector): Props =
    Props(new HumidityChecker(wemoConnector, netatmoConnector))

}

class HumidityChecker private (val wemoConnector: WemoConnector, val netatmoConnector: NetatmoConnector)
  extends Checker {

  val conf = ConfigFactory.load.getConfig("humidity")

  // Those values should be read from the database
  private val startTime: LocalTime = LocalTime.of(conf.getInt("startHour"), conf.getInt("stopMinute"))
  private val stopTime: LocalTime = LocalTime.of(conf.getInt("stopHour"), conf.getInt("stopMinute"))
  private val lowThreshold = conf.getInt("lowThreshold")
  private val highThreshold = conf.getInt("highThreshold")

  override def deviceName: String = conf.getString("deviceName")

  override def checkValue: Future[Float] = netatmoConnector.getHumidity

  private def inService(startTime: LocalTime, stopTime: LocalTime): Boolean = {
    val now = LocalTime.now()
    val b = now.isAfter(startTime) && now.isBefore(stopTime)
    log.debug(s"Humidity in service: {}", b)
    b
  }

  override def computeDesiredState(value: Float): Option[Boolean] = {
    inService(startTime, stopTime) match {
      case true => computeState(value, lowThreshold, highThreshold).map(!_) // We want to switch on the
        // deshumidifier if the value is too high
      case false => Some(false)
    }
  }
}
