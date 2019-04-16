package org.apache.james.gatling.jmap.scenari.realusage

import java.util.concurrent.ThreadLocalRandom

import io.gatling.core.Predef._
import io.gatling.core.feeder.{FeederBuilder, Record}
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.check.HttpCheck
import io.gatling.http.request.builder.HttpRequestBuilder
import org.apache.james.gatling.control.{UserFeeder, Username}
import org.apache.james.gatling.jmap._

class JmapInboxHomeLoadingScenario() {

  private object Keys {
    val inbox = "inboxID"
    val messageIds = "messageIds"
    val messagesDetailList = "messagesDetailList"
    val mainMailboxId = "mainMailboxId"
    val subMailboxIndex = "subMailboxIndex"
  }

  private val isSuccess: Seq[HttpCheck] = List(
    status.is(200),
    JmapChecks.noError)

  private val mailboxListPath = "[0][1].list"

  private val mailboxIdPath = s"$$$mailboxListPath[?(@.name == 'smbx$${subMailboxIndex}' && @.parentId == '$${mainMailboxId}')].id"

  private val listAllMailboxesAndSelectFirstOne: HttpRequestBuilder =
    JmapAuthentication.authenticatedQuery("getMailboxes", "/jmap")
      .body(StringBody("""[["getMailboxes",{},"#0"]]"""))
      .check(jsonPath(mailboxIdPath).find.saveAs(Keys.inbox))

  val listMessagesInInbox: HttpRequestBuilder =
    JmapAuthentication.authenticatedQuery("listMessagesInInbox", "/jmap")
      .body(StringBody(
        """[["getMessageList",
          |{"filter":{"inMailboxes":["${inboxID}"],
          |"text":null},
          |"sort":["date desc"],
          |"collapseThreads":false,
          |"fetchMessages":false,
          |"position":0,
          |"limit":30},"#0"]]""".stripMargin))
      .check(jsonPath("$[0][1].messageIds[*]").findAll.saveAs(Keys.messageIds))


  private val getMessages: HttpRequestBuilder = JmapAuthentication.authenticatedQuery("getMessages", "/jmap")
    .body(StringBody(
      """[["getMessages",
        |{"properties": ["id","blobId","threadId","headers","subject","from","to","cc","bcc","replyTo","preview","date","isUnread",
        |"isFlagged",
        |"isDraft",
        |"hasAttachment",
        |"mailboxIds",
        |"isAnswered",
        |"isForwarded"
        |]
        |,"ids": ${messageIds.jsonStringify()}},
        |"#0"]]""".stripMargin))

  private def addSelectedMailboxesToFeeder(record: Record[Any],
                                           mainMailboxesIdsByIndexForUsers: Map[Username, Map[Int, MailboxId]],
                                           nbMainMailboxes: Int,
                                           nbSubMailboxes: Int): Record[Any] = {
    val userName = record.get(UserFeeder.UsernameSessionParam)
    userName match {
      case Some(u: String) =>
        val mainMailboxIndex = ThreadLocalRandom.current().nextInt(nbMainMailboxes)
        val mainMailboxId = mainMailboxesIdsByIndexForUsers(Username(u))(mainMailboxIndex)
        val subMailboxIndex = ThreadLocalRandom.current().nextInt(nbSubMailboxes)
        record ++ Map(Keys.subMailboxIndex -> subMailboxIndex, Keys.mainMailboxId -> mainMailboxId.id)
      case _ => record
    }
  }

  def generate(feederBuilder: FeederBuilder,
               mainMailboxesIdsByIndexForUsers: => Map[Username, Map[Int, MailboxId]],
               nbMainMailboxes: Int,
               nbSubMailboxesPerMainOnes: Int): ScenarioBuilder = {
    scenario("JmapHomeLoadingScenario")
      .feed(() => feederBuilder().map(record => addSelectedMailboxesToFeeder(record, mainMailboxesIdsByIndexForUsers, nbMainMailboxes, nbSubMailboxesPerMainOnes)))
      .exec(CommonSteps.authentication())
      .group(InboxHomeLoading.name)(
        exec(RetryAuthentication.execWithRetryAuthentication(listAllMailboxesAndSelectFirstOne, JmapMailbox.getMailboxesChecks))
          .exec(RetryAuthentication.execWithRetryAuthentication(listMessagesInInbox, JmapMessages.listMessagesChecks))
          .exec(RetryAuthentication.execWithRetryAuthentication(getMessages, isSuccess)))

  }

}
