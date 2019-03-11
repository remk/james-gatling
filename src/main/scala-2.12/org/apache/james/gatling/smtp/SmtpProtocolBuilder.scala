package org.apache.james.gatling.smtp

object SmtpProtocolBuilder {
  val default = new SmtpProtocolBuilder(SmtpProtocol.default)
}

case class SmtpProtocolBuilder(protocol: SmtpProtocol) {

  def host(host: String) = copy(protocol.copy(host = host))
  def ssl(ssl: Boolean) = copy(protocol.copy(ssl = ssl))
  def port(port: Int) = copy(protocol.copy(port = port))
  def port(port: String) = Option(port).map(_ -> copy(protocol.copy(port = Integer.valueOf(port)))).getOrElse(this)
  def auth(auth: Boolean) = copy(protocol.copy(auth = auth))

  def build() = {
    Option(protocol.port).map(_ => protocol).getOrElse(port(SmtpProtocol.defaultPort(protocol.ssl)).protocol)
  }

}