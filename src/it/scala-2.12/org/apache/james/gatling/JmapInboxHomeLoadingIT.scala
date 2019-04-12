package org.apache.james.gatling

import org.apache.james.gatling.jmap.scenari.JmapInboxHomeLoadingScenario
import org.apache.james.gatling.jmap.{MessageId, RecipientAddress, Subject, TextBody}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class JmapInboxHomeLoadingIT extends JmapIT {
  before {
    Await.result(
      JMapFixtureCreator.authenticateAndCreateMail(server.james, Fixture.homer)(MessageId(), RecipientAddress(Fixture.bart), Subject(), TextBody())
        .map {
          response =>
            logger.debug(s"authenticateAndCreateMailResult : $response")
        }, 10 seconds)
  }
  scenario(feederBuilder => new JmapInboxHomeLoadingScenario().generate(feederBuilder))
}
