package org.apache.james.gatling.jmap.scenari

import io.gatling.core.Predef._
import io.gatling.core.feeder.FeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import org.apache.james.gatling.jmap.{CommonSteps, JmapMailboxes, JmapMessages, RetryAuthentication}

class JmapHomeLoadingScenario {

  def generate(feederBuilder: FeederBuilder): ScenarioBuilder = {
    val nbMessages = 30

    scenario("JmapHomeLoadingScenario")
      .feed(feederBuilder)
      .exec(CommonSteps.authentication())
      .exec(RetryAuthentication.execWithRetryAuthentication(JmapMailboxes.getMailboxes, JmapMailboxes.getMailboxesChecks))
      .exec(RetryAuthentication.execWithRetryAuthentication(JmapMessages.listFirstNthMessages(nbMessages), JmapMessages.listMessagesChecks))
  }

}
