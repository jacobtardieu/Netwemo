package io.tardieu.netwemo

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._

class ServerSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll with ScalatestRouteTest {

  implicit val actorSystem = ActorSystem()

  override def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  trait Fixtures {
    val checkScheduler = mock[CheckScheduler]
    val server = new Server(checkScheduler)
  }

  "The HTTP server" should
  "correctly schedule new Temperature check" in new Fixtures {
    Get("/temperature/deactivate?duration=42") ~> server.route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "ok"
      verify(checkScheduler).scheduleOrReplaceTemperature(42.minutes)
      verifyNoMoreInteractions(checkScheduler)
    }
  }
  it should "correctly schedule new Humidity check" in new Fixtures {
    Get("/humidity/deactivate?duration=13") ~> server.route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "ok"
      verify(checkScheduler).scheduleOrReplaceHumidity(13.minutes)
      verifyNoMoreInteractions(checkScheduler)
    }
  }

}
