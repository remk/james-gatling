package org.apache.james.gatling.jmap

import org.apache.james.gatling.Fixture
import org.apache.james.gatling.control.RandomUserPicker
import org.apache.james.gatling.jmap.scenari.JmapAllScenario

import scala.concurrent.duration._


class JmapAllIT extends JmapIT {

  before {
    users.foreach(server.sendMessage(Fixture.homer.username))
  }

  scenario(feederBuilder => {
    new JmapAllScenario().generate(feederBuilder, 10 seconds, RandomUserPicker(users))
  })
}
