package org.apache.james.gatling

import org.apache.james.gatling.jmap.scenari.realusage.{JMapUtils, JmapInboxHomeLoadingScenario}

import scala.concurrent.Await
import scala.concurrent.duration._

class JmapInboxHomeLoadingIT extends JmapIT(importMessages = true) {

  scenario(feederBuilder => {
    val oneMainMailbox = 1
    val oneSubMailbox = 1
    lazy val mainMailboxesIdsByIndexForUsers = Await.result(JMapUtils.mainMailboxesIdsByIndexForUsers(s"http://localhost:${mappedJmapPort}", users), 2 seconds)

    new JmapInboxHomeLoadingScenario().generate(feederBuilder, mainMailboxesIdsByIndexForUsers, oneMainMailbox, oneSubMailbox)
  })
}
