package semnorms.commands

import Query
import com.github.demidko.print.utils.printSeconds
import com.github.kotlintelegrambot.entities.ChatId
import semnorms.Executable
import semnorms.stem
import sendTempMessage

/** Semantic representation of balance request */
object Balance : Executable(
  stem(
    "time",
    "врем",
    "balance",
    "status",
    "score",
    "coins",
    "баланс",
    "статус",
    "счет",
    "счёт",
    "узна",
    "timecoin",
    "check"
  )
) {

  override fun execute(query: Query): Unit = query.run {

    val balance =
      message.from
        ?.id
        ?.let(storage::seconds)
        ?.printSeconds()
        ?: return@execute

    if (message.chat.id == message.from?.id) {
      bot.sendMessage(ChatId.fromId(message.chat.id), balance, replyToMessageId = message.messageId)
    } else {
      bot.sendTempMessage(message.chat.id, balance, replyToMessageId = message.messageId)
    }
  }
}