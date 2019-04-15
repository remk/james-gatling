package org.apache.james.gatling.simulation.utils

case class UsageFrequency(nbUsagePerHourPerUser: Int, nbUserPerHour: Int) {
  def paceFromFrequencyInMillis: Long = {
    nbUsagePerHourPerUser.toLong * nbUserPerHour * UsageFrequency.MILLIS_IN_ONE_HOUR
  }
}

object UsageFrequency {
  private val MILLIS_IN_ONE_HOUR = 3600 * 1000
  val ONE_TIME_PER_USER_PER_HOUR_WITH_FIVE_HUNDRED_USERS = UsageFrequency(1, 500)
}
