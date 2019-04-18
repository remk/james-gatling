package org.apache.james.gatling.jmap.scenari.realusage

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.apache.james.gatling.control.{User, Username}
import org.apache.james.gatling.jmap._
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws.DefaultBodyWritables._
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object JMapUtils {

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
      .addHttpHeaders(JmapHttp.HEADERS_JSON.toList: _*)
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
      .addHttpHeaders(JmapHttp.HEADERS_JSON.toList: _*)
      .post(body).map(r => (Json.parse(r.body) \ "accessToken").validate[String].get)
  }

  def mainMailboxesIdsByIndexForUsers(jmapUrl: String, users: Seq[User]): Future[Map[Username, Map[Int, MailboxId]]] = {
    Future.sequence(users.map(u =>
      mainMailboxesIdsByIndexForUser(jmapUrl, u).map(maiboxesByIndex => (u.username, maiboxesByIndex)))
    ).map(_.toMap)
  }

  def mainMailboxesIdsByIndexForUser(jmapUrl: String, user: User): Future[Map[Int, MailboxId]] = JMapUtils
    .authenticateAndListMailboxesWithFilterOnName(jmapUrl, user, name => name.name.startsWith("rmbx"))
    .map(_.map(mailbox => (getIndex(mailbox), mailbox.id)).toMap)

  private def listMailboxesWithFilterOnName(jmapUrl: String, accessToken: String, filterOnName: MailboxName => Boolean) =
    wsClient.url(jmapUrl + "/jmap")
      .addHttpHeaders((JmapHttp.HEADERS_JSON ++ Map("Authorization" -> accessToken)).toList: _*)
      .post("""[["getMailboxes",{},"#0"]]""").map {
      response =>
        (Json.parse(response.body) \ 0 \ 1 \ "list").validate[List[JmapMailbox]] match {
          case JsSuccess(mailboxes, _) =>
            mailboxes.filter(m => filterOnName(m.name))
        }
    }

  private def authenticateAndListMailboxesWithFilterOnName(jmap: String, user: User, filterOnName: MailboxName => Boolean) = {
    for {
      continuationToken <- getContinuationToken(jmap, user)
      accessToken <- obtainAccessToken(jmap, user, continuationToken)
      res <- listMailboxesWithFilterOnName(jmap, accessToken, filterOnName)
    } yield res
  }

  private def getIndex(mailbox: JmapMailbox) = {
    mailbox.name.name.replace("rmbx", "").toInt
  }

}
