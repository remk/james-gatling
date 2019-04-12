package org.apache.james.gatling.simulation.jmap

import io.gatling.core.Predef._
import org.apache.james.gatling.control.{UserCreator, UserFeeder}
import org.apache.james.gatling.jmap.scenari.JmapGetMailboxesScenario
import org.apache.james.gatling.simulation.{Configuration, HttpSettings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

class JmapGetMailboxesSimulation extends Simulation with DefaultPortMapping {

  private val users = Await.result(
    awaitable = Future.sequence(
      new UserCreator(Configuration.BaseJamesWebAdministrationUrl(getMappedPort)).createUsersWithInboxAndOutbox(Configuration.UserCount)),
    atMost = Inf)

  private val scenario = new JmapGetMailboxesScenario()

  setUp(scenario.generate(Configuration.ScenarioDuration)
    .feed(UserFeeder.toFeeder(users))
    .inject(atOnceUsers(Configuration.UserCount)))
    .protocols(HttpSettings.httpProtocol(getMappedPort))
}