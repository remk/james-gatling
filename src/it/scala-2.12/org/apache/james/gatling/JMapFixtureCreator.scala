package org.apache.james.gatling

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.james.gatling.control.User
import org.apache.james.gatling.jmap.{MessageId, RecipientAddress, Subject, TextBody}
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object JMapFixtureCreator {

  // Create Akka system for thread and streaming management
  implicit val system = ActorSystem()
  system.registerOnTermination {
    System.exit(0)
  }
  implicit val materializer = ActorMaterializer()
  val wsClient = StandaloneAhcWSClient()


  def getContinuationToken(jmapUrl: String, user: User): Future[String] = {
    val body =
      s"""{"username": "${user.username.value}",
          "clientName": "Mozilla Thunderbird",
          "clientVersion": "42.0",
          "deviceName": "Joe Blogg’s iPhone"}"""
    wsClient.url(s"$jmapUrl/authentication")
      .addHttpHeaders(Map("Content-Type" -> "application/json; charset=UTF-8", "Accept" -> "application/json").toList: _*)
      .post(
        body
      ).map(r => (Json.parse(r.body) \ "continuationToken").validate[String].get)
  }

  def obtainAccessToken(jmapUrl: String, user: User, continuationToken: String): Future[String] = {
    val body =
      s"""{"token": "$continuationToken",
          "method": "password",
          "password": "${user.password.value}"}"""
    wsClient.url(s"$jmapUrl/authentication")
      .addHttpHeaders(Map("Content-Type" -> "application/json; charset=UTF-8", "Accept" -> "application/json").toList: _*)
      .post(body).map(r => (Json.parse(r.body) \ "accessToken").validate[String].get)
  }

  def sendMessages(jmapUrl: String, continuationToken: String, accessToken: String, sender: User)(messageId: MessageId, recipientAddress: RecipientAddress, subject: Subject, textBody: TextBody) =
    wsClient.url(jmapUrl + "/jmap")
      .addHttpHeaders(Map("Content-Type" -> "application/json; charset=UTF-8", "Accept" -> "application/json",
        "Authorization" -> accessToken).toList: _*)
      .post(
        s"""[[
          "setMessages",
          {
            "create": {
              "${messageId.id}" : {
                "from": {"name":"${sender.username.value}", "email": "${sender.username.value}"},
                "to":  [{"name":"${recipientAddress.address}", "email": "${recipientAddress.address}"}],
                "textBody": "${textBody.text}",
                "subject": "${subject.subject}",
                "mailboxIds": []
              }
            }
          },
          "#0"
          ]]""")

  def authenticateAndCreateMail(jmap: String, sender: User)(messageId: MessageId, recipientAddress: RecipientAddress, subject: Subject, textBody: TextBody) = {
    for {
      continuationToken <- getContinuationToken(jmap, sender)
      accessToken <- obtainAccessToken(jmap, sender, continuationToken)
      res <- sendMessages(jmap, continuationToken, accessToken, sender)(messageId, recipientAddress, subject, textBody)
    } yield res
  }
}
