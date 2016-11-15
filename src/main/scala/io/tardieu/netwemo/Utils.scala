package io.tardieu.netwemo

import java.time.LocalTime

object Utils {

  def inBetween(value: LocalTime, startTime: LocalTime, stopTime: LocalTime): Boolean = {
    if (startTime.isBefore(stopTime)) {
      value.isAfter(startTime) && value.isBefore(stopTime)
    } else {
      value.isAfter(startTime) ^ value.isBefore(stopTime)
    }
  }

}
