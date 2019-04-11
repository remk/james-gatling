package org.apache.james.gatling

import org.apache.james.gatling.jmap.scenari.JmapHomeLoadingScenario

class JmapInboxHomeLoadingIT extends JmapIT {
  scenario(feederBuilder => new JmapHomeLoadingScenario().generate(feederBuilder))
}
