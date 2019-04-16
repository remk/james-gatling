package org.apache.james.gatling

import org.apache.james.gatling.jmap.scenari.realusage.{JMapUtils, JmapInboxHomeLoadingScenario}

import scala.concurrent.Await
import scala.concurrent.duration._

class JmapInboxHomeLoadingIT extends JmapIT {

  private def execCommand(command: String) = {
    val rt = Runtime.getRuntime
    val copyPr = rt.exec(command)
    copyPr.waitFor()
  }

  before {
    val emlPath = getClass.getResource("/message.eml").getPath
    val shPath = getClass.getResource("/create_mails_and_mailboxes.sh").getPath

    execCommand(s"docker cp  ${emlPath} ${server.containerId}:/message.eml")
    execCommand(s"docker cp ${shPath} ${server.containerId}:/create_mails_and_mailboxes.sh")
    execCommand(s"docker exec ${server.containerId}  /create_mails_and_mailboxes.sh")
    Thread.sleep(1000)
  }

  scenario(feederBuilder => {
    val oneMainMailbox = 1
    val oneSubMailbox = 1
    lazy val mainMailboxesIdsByIndexForUsers = Await.result(JMapUtils.mainMailboxesIdsByIndexForUsers(s"http://localhost:${server.mappedJmapPort}", users), 2 seconds)

    new JmapInboxHomeLoadingScenario().generate(feederBuilder, mainMailboxesIdsByIndexForUsers, oneMainMailbox, oneSubMailbox)
  })
}
