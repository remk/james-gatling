package org.apache.james.gatling.jmap

import org.apache.james.gatling.Fixture
import org.apache.james.gatling.control.{JamesWebAdministrationQuery, RandomUserPicker, UserFeeder}
import org.apache.james.gatling.jmap.scenari.JmapQueueBrowseScenario

import scala.concurrent.duration._

class JmapQueueBrowseIT extends JmapIT {

  var webAdmin = new JamesWebAdministrationQuery(server.mappedWebadmin.baseUrl)

  before {
    users.foreach(server.sendMessage(Fixture.homer.username))
  }

  scenario(feederBuilder => {
    new JmapQueueBrowseScenario().generate(10 seconds, UserFeeder.toFeeder(users), RandomUserPicker(users), webAdmin)
  })
}
