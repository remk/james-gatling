package org.apache.james.gatling.jmap.scenari.realusage

sealed trait RealUsageScenario {
  def name : String
}

case object InboxHomeLoading extends RealUsageScenario {
  def name = "InboxHomeLoading"
}
