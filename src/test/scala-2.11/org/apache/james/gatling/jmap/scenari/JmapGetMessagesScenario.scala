package org.apache.james.gatling.jmap.scenari

import io.gatling.core.Predef._
import org.apache.james.gatling.control.UserCreator
import org.apache.james.gatling.jmap.scenari.common.CommonSteps
import org.apache.james.gatling.jmap.{HttpSettings, JmapMessages}

import scala.concurrent.duration._

class JmapGetMessagesScenario extends Simulation {


  val userCount = 100

  val users = UserCreator.createUsersWithInboxAndOutbox(userCount)

  val scn = scenario("JmapGetMessages")
    .exec(CommonSteps.provisionUsersWithMessageList(users))
    .repeat(250, "any") {
      exec(JmapMessages.getRandomMessage())
        .pause(1 second , 2 seconds)
    }

  setUp(scn.inject(atOnceUsers(userCount))).protocols(HttpSettings.httpProtocol)

}
