package io.tardieu.netwemo.checkers

import akka.actor.{Actor, ActorLogging}
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

case object RunCheck

trait Checker extends Actor with ActorLogging {

  def wemoConnector: WemoConnector

  def netatmoConnector: NetatmoConnector

  def deviceName: String

  def checkValue: Future[Float]

  def computeDesiredState(value: Float): Option[Boolean]

  def activate(): Unit = {
    log.debug("Activating check for {}", deviceName)
    checkValue.map(computeDesiredState).onComplete {
      case Success(Some(true)) => wemoConnector.switchOn(deviceName)
      case Success(Some(false)) => wemoConnector.switchOff(deviceName)
      case Success(None) => // We leave the switch in the same state
      case Failure(ex) =>
        log.error(ex, s"Exception in computing desired state, switching off")
        wemoConnector.switchOff(deviceName)
    }
  }

  def computeState(value: Float, lowThreshold: Float, highThreshold: Float): Option[Boolean] = {
    value match {
      case _ if value < lowThreshold =>
        log.debug(s"Device {}: {} < low threshold {}", deviceName, value, lowThreshold)
        Some(true)
      case _ if value > highThreshold =>
        log.debug(s"Device {}: {} > high threshold {}", deviceName, value, highThreshold)
        Some(false)
      case _ =>
        log.debug(s"Device {}: {} between low threshold {} and high threshold {}",
          deviceName, value, lowThreshold, highThreshold)
        None
    }
  }

  def receive = {
    case RunCheck => activate()
  }

}
