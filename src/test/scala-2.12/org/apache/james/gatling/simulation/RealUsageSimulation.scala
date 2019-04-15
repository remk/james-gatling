package org.apache.james.gatling.simulation

import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import org.apache.james.gatling.control.{Password, User, UserFeeder, Username}
import org.apache.james.gatling.jmap.scenari.realusage.{InboxHomeLoading, JmapInboxHomeLoadingScenario, RealUsageScenario}
import org.apache.james.gatling.simulation.jmap.DefaultPortMapping
import org.apache.james.gatling.simulation.utils.UsageFrequency
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.duration._

class RealUsageSimulation extends Simulation with DefaultPortMapping {
  private val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val NB_USERS = 100
  private val NB_SIMULATED_USERS = 50000
  private val USER_RATIO = NB_SIMULATED_USERS / NB_USERS

  private def paceCorrectedInMillis(frequency: UsageFrequency) = {
    frequency.paceFromFrequencyInMillis * USER_RATIO
  }

  private val getUsers: Seq[User] = (0 until 100).map(i => User(Username(s"user$i@open-paas.org"), Password("secret")))
  
  private val feeder: SourceFeederBuilder[String] = UserFeeder.toFeeder(getUsers).circular

  private def buildPerfScenario(scenarioName: String, scenarioBuilder: ScenarioBuilder, usageFrequency: UsageFrequency) = scenario(scenarioName)
    .forever(
      pace(paceCorrectedInMillis(usageFrequency) millis).exec(scenarioBuilder)
    )
    .inject(rampUsers(NB_USERS) during (1 minute))
    .protocols(HttpSettings.httpProtocol(getMappedPort: Int => Int))


  private def buildMaxScenarioResponseTimeAssertion(scenario: RealUsageScenario, maxResponseTime: Duration) = {
    details(scenario.name).responseTime.max.lt(maxResponseTime.toMillis.toInt)
  }

  private val inboxHomeLoadingScenario = buildPerfScenario("Inbox home loading", new JmapInboxHomeLoadingScenario().generate(feeder), UsageFrequency.ONE_TIME_PER_USER_PER_HOUR_WITH_FIVE_HUNDRED_USERS)

  setUp(inboxHomeLoadingScenario)
    .assertions(buildMaxScenarioResponseTimeAssertion(InboxHomeLoading, 2 seconds))

}
