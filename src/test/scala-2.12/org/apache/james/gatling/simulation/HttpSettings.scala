package org.apache.james.gatling.simulation

import io.gatling.core.Predef._
import io.gatling.http.Predef.http

object HttpSettings {

  def httpProtocol(getMappedPort : Int => Int) = http
    .baseUrl(Configuration.BaseJmapUrl(getMappedPort))
    .acceptHeader("application/json")
    .contentTypeHeader("application/json; charset=UTF-8")

}
