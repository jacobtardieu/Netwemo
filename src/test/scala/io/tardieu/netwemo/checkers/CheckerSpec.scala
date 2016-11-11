package io.tardieu.netwemo.checkers

import akka.actor.{ActorSystem, Props}
import akka.testkit.TestActorRef
import io.tardieu.netwemo.connectors.{NetatmoConnector, WemoConnector}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.Future

class CheckerSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfterAll {

  implicit val actorSystem = ActorSystem()

  override def afterAll(): Unit = {
    actorSystem.terminate()
    super.afterAll()
  }

  trait Fixtures {
    class MockChecker(
                       stateReturn: Option[Boolean] = Some(true),
                       override val checkValue: Future[Float] = Future.successful(19.6.toFloat)
                     ) extends Checker {
      override val wemoConnector = mock[WemoConnector]
      override val netatmoConnector = mock[NetatmoConnector]
      override val deviceName = "test"
      override def computeDesiredState(value: Float) = stateReturn
    }
  }

  "The Checker trait" should
  "correctly compute the state" in new Fixtures {
    val checkerTestActor = TestActorRef[MockChecker](Props(new MockChecker))
    checkerTestActor.underlyingActor.computeState(20.1.toFloat, 19, 21) shouldBe empty
    checkerTestActor.underlyingActor.computeState(21.1.toFloat, 19, 21) should contain (false)
    checkerTestActor.underlyingActor.computeState(14.3.toFloat, 19, 21) should contain (true)
  }
  it should "call the switchOn method when state should be on" in new Fixtures {
    val checkerTestActor = TestActorRef[MockChecker](Props(new MockChecker(Some(true))))
    checkerTestActor ! RunCheck
    verify(checkerTestActor.underlyingActor.wemoConnector).switchOn("test")
    verifyNoMoreInteractions(checkerTestActor.underlyingActor.wemoConnector)
  }
  it should "call the switchOff method when state should be off" in new Fixtures {
    val checkerTestActor = TestActorRef[MockChecker](Props(new MockChecker(Some(false))))
    checkerTestActor ! RunCheck
    verify(checkerTestActor.underlyingActor.wemoConnector).switchOff("test")
    verifyNoMoreInteractions(checkerTestActor.underlyingActor.wemoConnector)
  }
  it should "do nothing when the state should not be changed" in new Fixtures {
    val checkerTestActor = TestActorRef[MockChecker](Props(new MockChecker(None)))
    checkerTestActor ! RunCheck
    verifyZeroInteractions(checkerTestActor.underlyingActor.wemoConnector)
  }
  it should "switch off the device in case of exception" in new Fixtures {
    val checkerTestActor =
      TestActorRef[MockChecker](Props(new MockChecker(checkValue = Future.failed(new RuntimeException("Some error")))))
    checkerTestActor ! RunCheck
    verify(checkerTestActor.underlyingActor.wemoConnector).switchOff("test")
    verifyNoMoreInteractions(checkerTestActor.underlyingActor.wemoConnector)
  }

}
