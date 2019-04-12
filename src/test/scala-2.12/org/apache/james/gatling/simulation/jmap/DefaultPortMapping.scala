package org.apache.james.gatling.simulation.jmap

trait DefaultPortMapping {
  protected val getMappedPort: Int => Int = identity
}
