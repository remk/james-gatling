package org.apache.james.gatling.simulation.jmap

import io.gatling.core.Predef._
import org.apache.james.gatling.control._
import org.apache.james.gatling.jmap.scenari.FeederJmapAllScenario
import org.apache.james.gatling.simulation.{Configuration, HttpSettings}

class FeederJmapAllSimulation extends Simulation with DefaultPortMapping {

  private def recordValueToString(recordValue: Any): String = recordValue match {
    case s: String => s
    case a: Any => println("Warning: calling toString on a feeder value"); a.toString
  }

  private val users: Seq[User] = csv("users.csv").readRecords
    .map(record =>
      User(
        username = Username(recordValueToString(record("username"))),
        password = Password(recordValueToString(record("password")))))

  private val scenario = new FeederJmapAllScenario()

  setUp(scenario
    .generate(Configuration.ScenarioDuration, RandomUserPicker(users))
    .feed(UserFeeder.toFeeder(users))
    .inject(atOnceUsers(Configuration.UserCount)))
    .protocols(HttpSettings.httpProtocol(getMappedPort))
}