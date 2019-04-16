package org.apache.james.gatling.utils

object MailboxNameGenerator {

  def mainMailbox(num: Int): String = s"rmbx$num"

  def mainSubMailbox(mainNum: Int, subNum: Int): String = s"rmbx${mainNum}.smbx$subNum"

}
