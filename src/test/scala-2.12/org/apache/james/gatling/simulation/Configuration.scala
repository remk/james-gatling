package org.apache.james.gatling.simulation

import java.net.URL

import scala.concurrent.duration._

object Configuration {

  val ServerHostName = "127.0.0.1"

  def BaseJmapUrl(getMappedPort: Int => Int) = s"http://$ServerHostName:${getMappedPort(80)}"

  def BaseJamesWebAdministrationUrl(getMappedPort: Int => Int) = new URL(s"http://$ServerHostName:${getMappedPort(8000)}")

  val ScenarioDuration = 3 hours
  val UserCount = 1000
  val RandomlySentMails = 10
  val NumberOfMailboxes = 10
  val NumberOfMessages = 20

}
