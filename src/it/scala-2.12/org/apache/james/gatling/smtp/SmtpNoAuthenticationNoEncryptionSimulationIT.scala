package org.apache.james.gatling.smtp

import org.apache.james.gatling.smtp.scenari.SmtpNoAuthenticationNoEncryptionScenario

import scala.concurrent.duration._

class SmtpNoAuthenticationNoEncryptionSimulationIT extends SmtpIT {
  scenario(feederBuilder => new SmtpNoAuthenticationNoEncryptionScenario().generate(10.seconds, feederBuilder))
}
