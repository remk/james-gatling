package org.apache.james.gatling.smtp

import org.apache.james.gatling.smtp.scenari.SmtpNoAuthenticationNoEncryptionBigBodyScenario

import scala.concurrent.duration._

class SmtpNoAuthenticationNoEncryptionBigBodySimulationIT extends SmtpIT {
  scenario(feederBuilder => new SmtpNoAuthenticationNoEncryptionBigBodyScenario().generate(10.seconds, feederBuilder))
}
