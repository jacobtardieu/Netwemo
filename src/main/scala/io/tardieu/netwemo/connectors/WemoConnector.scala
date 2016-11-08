package io.tardieu.netwemo.connectors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
  * Handles the connection to Wemo device.
  * Uses the REST API of Ouimeaux (http://ouimeaux.readthedocs.org/) to control the switch.
  * The Ouimeaux server must run in the same network than the switch.
  */
class WemoConnector(implicit val system: ActorSystem) {

  private[this] val logger = LoggerFactory.getLogger(getClass)

  private[this] implicit val materializer = ActorMaterializer()
  private[this] implicit val executionContext = system.dispatcher

  private[this] val conf = ConfigFactory.load.getConfig("wemo")
  private[this] val wemoHost = conf.getString("host")
  private[this] val wemoPort = conf.getInt("port")

  def switchOn(device: String): Unit = switchState("on", device)
  def switchOff(device: String): Unit = switchState("off", device)
  def toggle(device: String): Unit = switchState("toggle", device)

  private def switchState(state: String, device: String): Unit = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = Uri(s"http://$wemoHost:$wemoPort/api/device/$device").withQuery(Query("state" -> state))
    )

    Http().singleRequest(request).onComplete {
      case Success(response) =>
        val message = Unmarshal(response.entity).to[String]
        if (response.status.isFailure()) {
          message.foreach(logger.error(s"HTTP error {}: {}", response.status.asInstanceOf[Any], _))
        }
        else {
          logger.debug(
            "Switch {} order for {} received with status {}",
            state, device, response.status)
          logger.trace(
            "Switch {} order for {} received with status {}: {}",
            state, device, response.status, message)
        }
      case Failure(e) =>
        logger.error("Error contacting the wemo server", e)
    }
  }

}
