package org.apache.james.gatling.jmap.scenari

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import org.apache.james.gatling.jmap.CommonSteps.UserPicker
import org.apache.james.gatling.jmap.{CommonSteps, JmapMailbox, JmapMessages, MessageId}

import scala.concurrent.duration._

class FeederJmapAllScenario {
  private val loopVariableName = "any"

  def generate(duration: Duration, feederBuilder: FeederBuilder, userPicker: UserPicker): ScenarioBuilder =
    scenario("FeederJmapAllScenarios")
      .feed(feederBuilder)
      .during(duration) {
        exec(CommonSteps.authentication())
        .exec(JmapMessages.sendMessagesToUserWithRetryAuthentication(userPicker, session => MessageId()))
        .pause(1 second, 5 seconds)
        .exec(JmapMailbox.getSystemMailboxesWithRetryAuthentication)
        .exec(JmapMessages.listMessagesWithRetryAuthentication())
        .exec(JmapMessages.getMessagesWithRetryAuthentication())
        .pause(1 second, 5 seconds)
        .randomSwitch(
          70.0 -> exec(JmapMessages.markAsRead()),
          20.0 -> exec(JmapMessages.markAsAnswered()),
          10.0 -> exec(JmapMessages.markAsFlagged())
        )
    }

}
