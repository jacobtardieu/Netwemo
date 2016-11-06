package io.tardieu.netwemo

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.LogEntry
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

/**
  * HTTP server receiving the commands sent to Netwemo
  */
class Server(checkScheduler: CheckScheduler)(implicit actorSystem: ActorSystem) {

  private[this] implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  private[this] implicit val executionContext = actorSystem.dispatcher

  private[this] val conf = ConfigFactory.load().getConfig("server")
  private[this] val host = conf.getString("host")
  private[this] val port = conf.getInt("port")

  private[this] val route =
    path("temperature" / "deactivate") {
      get {
        parameter('duration.as[Int]) { duration =>
          checkScheduler.scheduleOrReplaceTemperature(duration.minutes)
          complete("ok")
        }
      }
    } ~
      path("humidity" / "deactivate") {
        get {
          parameter('duration.as[Int]) { duration =>
            checkScheduler.scheduleOrReplaceHumidity(duration.minutes)
            complete("ok")
          }
        }
      }

  def requestMethod(req: HttpRequest): LogEntry =
    LogEntry(s"${req.method.name} request: ${req.uri}", Logging.DebugLevel)

  private[this] val loggingRoute = logRequest(requestMethod _)(route)

  private[this] val bindingFuture = Http().bindAndHandle(loggingRoute, host, port)

  def stopServer() = bindingFuture.foreach(_.unbind())

}
