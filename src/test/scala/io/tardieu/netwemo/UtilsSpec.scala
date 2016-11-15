package io.tardieu.netwemo

import java.time.LocalTime

import org.scalatest.{FlatSpec, Matchers}

class UtilsSpec extends FlatSpec with Matchers {

  "The inBetween method" should
  "work with startTime before stopTime" in {
    val startTime = LocalTime.parse("08:00")
    val stopTime = LocalTime.parse("18:00")
    val before = LocalTime.parse("06:54")
    val during = LocalTime.parse("14:43")
    val after = LocalTime.parse("20:22")
    Utils.inBetween(before, startTime, stopTime) shouldBe false
    Utils.inBetween(during, startTime, stopTime) shouldBe true
    Utils.inBetween(after, startTime, stopTime) shouldBe false
  }
  it should "work with startTime after stopTime" in {
    val startTime = LocalTime.parse("22:00")
    val stopTime = LocalTime.parse("02:00")
    val before = LocalTime.parse("21:54")
    val during1 = LocalTime.parse("22:43")
    val during2 = LocalTime.parse("00:32")
    val after = LocalTime.parse("08:11")
    Utils.inBetween(before, startTime, stopTime) shouldBe false
    Utils.inBetween(during1, startTime, stopTime) shouldBe true
    Utils.inBetween(during2, startTime, stopTime) shouldBe true
    Utils.inBetween(after, startTime, stopTime) shouldBe false
  }

}
