package org.apache.james.gatling.simulation

import io.gatling.core.Predef._
import io.gatling.core.feeder.SourceFeederBuilder
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.ScenarioBuilder
import org.apache.james.gatling.control.{Password, User, UserFeeder, Username}
import org.apache.james.gatling.jmap.scenari.realusage.{InboxHomeLoading, JMapUtils, JmapInboxHomeLoadingScenario, RealUsageScenario}
import org.apache.james.gatling.simulation.jmap.DefaultPortMapping
import org.apache.james.gatling.simulation.utils.UsageFrequency
import org.slf4j
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration._

class RealUsageSimulation extends Simulation with DefaultPortMapping {
  private val logger: slf4j.Logger = LoggerFactory.getLogger(this.getClass.getCanonicalName)

  private val NB_USERS = 100
  private val NB_MAIN_MAILBOXES_PER_USER = 10
  private val NB_SUB_MAILBOXES_PER_MAIN_ONES = 10

  private val JMAP_PORT = 80

  private val jmapUrl = s"http://localhost:${getMappedPort(JMAP_PORT)}"
  private val getUsers: Seq[User] = (0 until NB_USERS).map(i => User(Username(s"user$i@open-paas.org"), Password("secret")))

  private val feeder: SourceFeederBuilder[String] = UserFeeder.toFeeder(getUsers).circular

  private def buildPerfScenario(scenarioName: String, scenarioBuilder: ScenarioBuilder, usageFrequency: UsageFrequency) = scenario(scenarioName)
    .forever(
      pace(usageFrequency.paceFromFrequency).exec(scenarioBuilder)
    )
    .inject(rampUsers(NB_USERS) during (1 minute))
    .protocols(HttpSettings.httpProtocol(getMappedPort: Int => Int))


  private def buildMaxScenarioResponseTimeAssertion(scenario: RealUsageScenario, maxResponseTime: Duration) = {
    details(scenario.name).responseTime.max.lt(maxResponseTime.toMillis.toInt)
  }

  private val inboxHomeLoadingScenario = {
    val mainMailboxesIdsByIndexForUsers = Await.result(JMapUtils.mainMailboxesIdsByIndexForUsers(jmapUrl, getUsers), 2 seconds)
    buildPerfScenario("Inbox home loading",
      new JmapInboxHomeLoadingScenario().generate(feeder, mainMailboxesIdsByIndexForUsers, NB_MAIN_MAILBOXES_PER_USER, NB_SUB_MAILBOXES_PER_MAIN_ONES),
      UsageFrequency.ONE_TIME_PER_USER_PER_HOUR_FOR_FIFTY_THOUSANDS_USERS
    )
  }
  setUp(inboxHomeLoadingScenario)
    .assertions(buildMaxScenarioResponseTimeAssertion(InboxHomeLoading, 2 seconds))

}
