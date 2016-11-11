package io.tardieu.netwemo

import akka.actor.ActorSystem
import akka.testkit.TestProbe
import io.tardieu.netwemo.checkers.RunCheck
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.duration._

class CheckSchedulerSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll{

  implicit val actorSystem = ActorSystem()

  override def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  trait Fixtures {
    val netatmoConnector = mock[NetatmoConnector]
    val wemoConnector = mock[WemoConnector]
    val temperatureChecker = TestProbe()
    val humidityChecker = TestProbe()
    val checkScheduler = new CheckScheduler(
      wemoConnector, netatmoConnector,
      temperatureChecker.testActor, humidityChecker.testActor)
  }

  "The CheckScheduler" should
  "correctly create a temperature schedule" in new Fixtures {
    checkScheduler.tempSchedule shouldBe empty
    checkScheduler.scheduleOrReplaceTemperature(1.hour)
    checkScheduler.tempSchedule shouldBe defined
    checkScheduler.tempSchedule.map(_.isCancelled) should contain (false)
  }
  it should "correctly cancel a temperature schedule" in new Fixtures {
    checkScheduler.scheduleOrReplaceTemperature(1.second)
    val schedule = checkScheduler.tempSchedule
    checkScheduler.cancelTemperatureSchedule()
    schedule.map(_.isCancelled) should contain (true)
    checkScheduler.tempSchedule shouldBe empty
    temperatureChecker.expectNoMsg(2.seconds)
  }
  it should "correctly replace a temperature schedule" in new Fixtures {
    checkScheduler.scheduleOrReplaceTemperature(1.hour)
    val previousSchedule = checkScheduler.tempSchedule
    checkScheduler.scheduleOrReplaceTemperature(2.hours)
    previousSchedule.map(_.isCancelled) should contain (true)
    checkScheduler.tempSchedule shouldBe defined
    checkScheduler.tempSchedule.map(_.isCancelled) should contain (false)
  }
  it should "correctly send messages to the temperatureChecker actor" in new Fixtures {
    checkScheduler.scheduleOrReplaceTemperature(1.second)
    temperatureChecker.expectNoMsg(900.millis)
    temperatureChecker.expectMsg(2.seconds, RunCheck)
    temperatureChecker.expectMsg(2.seconds, RunCheck)
  }

  it should "correctly create a humidity schedule" in new Fixtures {
    checkScheduler.humiditySchedule shouldBe empty
    checkScheduler.scheduleOrReplaceHumidity(1.hour)
    checkScheduler.humiditySchedule shouldBe defined
    checkScheduler.humiditySchedule.map(_.isCancelled) should contain (false)
  }
  it should "correctly cancel a humidity schedule" in new Fixtures {
    checkScheduler.scheduleOrReplaceHumidity(1.second)
    val schedule = checkScheduler.humiditySchedule
    checkScheduler.cancelHumiditySchedule()
    schedule.map(_.isCancelled) should contain (true)
    checkScheduler.humiditySchedule shouldBe empty
    humidityChecker.expectNoMsg(2.seconds)
  }
  it should "correctly replace a humidity schedule" in new Fixtures {
    checkScheduler.scheduleOrReplaceHumidity(1.hour)
    val previousSchedule = checkScheduler.humiditySchedule
    checkScheduler.scheduleOrReplaceHumidity(2.hours)
    previousSchedule.map(_.isCancelled) should contain (true)
    checkScheduler.humiditySchedule shouldBe defined
    checkScheduler.humiditySchedule.map(_.isCancelled) should contain (false)
  }
  it should "correctly send messages to the humidityChecker actor" in new Fixtures {
    checkScheduler.scheduleOrReplaceHumidity(1.second)
    humidityChecker.expectNoMsg(900.millis)
    humidityChecker.expectMsg(2.seconds, RunCheck)
    humidityChecker.expectMsg(2.seconds, RunCheck)
  }

}
