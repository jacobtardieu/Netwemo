package io.tardieu.netwemo.connectors

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}


/**
  * Object containing the different metrics available
  */
object Metric extends Enumeration {
  type MetricType = Value
  val Humidity, Temperature = Value
}

final case class Token(access_token: String, expires_in: Long)
final case class Measure(value: Seq[Seq[Float]])
final case class NetatmoResponse(status: String, body: Seq[Measure], time_exec: Float)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val tokenFormat = jsonFormat2(Token)
  implicit val measureFormat = jsonFormat1(Measure)
  implicit val netatmoResponseFormat = jsonFormat3(NetatmoResponse)
}

/**
  * Handles connection to Netatmo REST API.
  */
class NetatmoConnector(implicit val system: ActorSystem) extends JsonSupport {

  private[this] implicit val materializer = ActorMaterializer()

  private[this] val conf = ConfigFactory.load.getConfig("netatmo")

  private[this] val tokenUri = conf.getString("token_uri")
  private[this] val measureUri = conf.getString("measure_uri")

  private[this] val client_id = conf.getString("client_id")
  private[this] val client_secret = conf.getString("client_secret")
  private[this] val refresh_token = conf.getString("refresh_token")
  private[this] val device_id = conf.getString("device_id")

  private[this] val postRefreshParameters = Map(
    "grant_type" -> "refresh_token",
    "refresh_token" -> refresh_token,
    "client_id" -> client_id,
    "client_secret" -> client_secret
  )


  private[this] def refreshToken: Future[Token] = {
    val request = HttpRequest(
      method = HttpMethods.POST,
      uri = tokenUri,
      entity = FormData(postRefreshParameters).toEntity,
      headers = List(`Accept`(MediaTypes.`application/json`))
    )

    Http().singleRequest(request).flatMap { response =>
      response.status match {
        case status if status.isFailure() =>
          val message = Await.result(Unmarshal(response.entity).to[String], 1.second)
          throw new RuntimeException(s"HTTP error $status: $message")
        case _ =>
          Unmarshal(response.entity).to[Token]
      }
    }
  }

  def getMetric(metricType: Metric.MetricType): Future[Float] = {
    val token = refreshToken // TODO: Do it only when required
    val response = token.flatMap{ t =>
      val params = Map(
        "access_token" -> t.access_token,
        "device_id" -> device_id,
        "scale" -> "max",
        "date_end" -> "last",
        "type" -> metricType.toString
      )
      val request = HttpRequest(
        uri = Uri(measureUri).withQuery(Query(params)),
        headers = List(`Accept`(MediaTypes.`application/json`))
      )
      Http().singleRequest(request)
    }

    val netatmoResponse = response.flatMap{ r =>
      r.status match {
        case status if status.isFailure() =>
          val message = Await.result(Unmarshal(r.entity).to[String], 1.second)
          throw new RuntimeException(s"HTTP error $status: $message")
        case _ =>
          Unmarshal(r.entity).to[NetatmoResponse]
      }
    }
    netatmoResponse.map(_.body.head.value.head.head)
  }

  def getHumidity = getMetric(Metric.Humidity)
  def getTemperature = getMetric(Metric.Temperature)

}
