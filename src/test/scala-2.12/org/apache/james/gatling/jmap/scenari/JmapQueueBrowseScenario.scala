package org.apache.james.gatling.jmap.scenari

import io.gatling.core.Predef.{atOnceUsers, clock, exec, openInjectionProfileFactory, randomSwitch, scenario}
import io.gatling.core.scenario.Simulation
import org.apache.james.gatling.control.{JamesWebAdministrationQuery, UserCreator}
import org.apache.james.gatling.jmap.JmapMessages
import org.apache.james.gatling.jmap.scenari.common.Configuration._
import org.apache.james.gatling.jmap.scenari.common.{CommonSteps, HttpSettings}

class JmapQueueBrowseScenario extends Simulation {

  val users = new UserCreator(BaseJamesWebAdministrationUrl).createUsersWithInboxAndOutbox(UserCount)

  val webAdmin = new JamesWebAdministrationQuery(BaseJamesWebAdministrationUrl)

  val scn = scenario("JmapSendMessages")
    .exec(CommonSteps.provisionSystemMailboxes(users))
    .during(ScenarioDuration) {
      exec(
        randomSwitch(
          99.0 -> JmapMessages.sendMessagesRandomlyWithRetryAuthentication(users),
          1.0 -> webAdmin.getMailQueueMails("spool")))
    }

  setUp(scn.inject(atOnceUsers(UserCount))).protocols(HttpSettings.httpProtocol)

}
