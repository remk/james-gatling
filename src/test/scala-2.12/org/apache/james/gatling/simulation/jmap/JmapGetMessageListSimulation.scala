package org.apache.james.gatling.simulation.jmap

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import org.apache.james.gatling.control.{RandomUserPicker, UserCreator, UserFeeder}
import org.apache.james.gatling.jmap.scenari.JmapGetMessageListScenario
import org.apache.james.gatling.simulation.{Configuration, HttpSettings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration.Inf
import scala.concurrent.{Await, Future}

class JmapGetMessageListSimulation(getMappedPort : Int => Int = identity) extends Simulation {

  private val users = Await.result(
    awaitable = Future.sequence(
      new UserCreator(Configuration.BaseJamesWebAdministrationUrl(getMappedPort)).createUsersWithInboxAndOutbox(Configuration.UserCount)),
    atMost = Inf)

  private val scenario = new JmapGetMessageListScenario()

  setUp(scenario
    .generate(Configuration.ScenarioDuration, RandomUserPicker(users), Configuration.RandomlySentMails)
      .feed(UserFeeder.toFeeder(users))
      .inject(atOnceUsers(Configuration.UserCount)))
    .protocols(HttpSettings.httpProtocol(getMappedPort))
}