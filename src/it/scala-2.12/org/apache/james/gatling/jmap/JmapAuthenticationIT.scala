package org.apache.james.gatling.jmap

import org.apache.james.gatling.jmap.scenari.JmapAuthenticationScenario

class JmapAuthenticationIT extends JmapIT {
  scenario(feederBuilder => new JmapAuthenticationScenario().generate(feederBuilder))
}
