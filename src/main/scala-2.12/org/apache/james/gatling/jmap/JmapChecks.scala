package org.apache.james.gatling.jmap

import io.gatling.core.Predef._
import io.gatling.core.session.Expression

object JmapChecks {

  val noError = jsonPath("$.error").notExists

  def created(messageId: Expression[MessageId]) = jsonPath(s"$$[0][1].created['${messageId}'].id").exists

}
