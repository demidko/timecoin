import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.logging.LogLevel.Error
import org.slf4j.LoggerFactory.getLogger
import semnorms.Executable
import java.lang.System.currentTimeMillis
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule
import kotlin.time.Duration

private val log = getLogger("Timecobot")
private val timer = Timer()

fun Bot(tok: String, coins: Timecoins, pins: PinnedMessages) = bot {
  token = tok
  logLevel = Error
  log.info("Tok: $tok")
  dispatch {
    text {
      log.info(text)
      val timestamp = currentTimeMillis()
      try {
        message.from?.id?.let(coins::register)
        bot.execute(text, message, coins, pins)
      } catch (e: RuntimeException) {
        log.error(text, e)
      } finally {
        val elapsedMs = currentTimeMillis() - timestamp
        if (elapsedMs > 500) {
          log.warn("Too large message processed (${elapsedMs}ms): $text")
        }
      }
    }
  }
}

/**
 * Use this method to send text messages
 * @param chatId unique identifier for the target chat
 * @param text text of the message to be sent, 1-4096 characters after entities parsing
 * @param parseMode mode for parsing entities in the message text
 * @param disableWebPagePreview disables link previews for links in this message
 * @param disableNotification sends the message silently - users will receive a notification with no sound
 * @param replyToMessageId if the message is a reply, ID of the original message
 * @param replyMarkup additional options - inline keyboard, custom reply keyboard, instructions to remove reply
 * keyboard or to force a reply from the user
 * @param lifetime message lifetime
 */
fun Bot.sendTempMessage(
  chatId: Long,
  text: String,
  parseMode: ParseMode? = null,
  disableWebPagePreview: Boolean? = null,
  disableNotification: Boolean? = true,
  replyToMessageId: Long? = null,
  replyMarkup: ReplyMarkup? = null,
  lifetime: Duration = Duration.seconds(15)
) {

  val messageSendingResult = sendMessage(
    ChatId.fromId(chatId),
    text,
    parseMode,
    disableWebPagePreview,
    disableNotification,
    replyToMessageId,
    replyMarkup
  )

  val messageId = messageSendingResult.first
    ?.body()
    ?.result
    ?.messageId
    ?: error("Failed to send message")

  delayDeleteMessage(chatId, messageId, lifetime)
}

/**
 * Use this method to delete a message, including service messages, with the following limitations:
 * - A message can only be deleted if it was sent less than 48 hours ago.
 * - A dice message in a private chat can only be deleted if it was sent more than 24 hours ago.
 * - Bots can delete outgoing messages in private chats, groups, and supergroups.
 * - Bots can delete incoming messages in private chats.
 * - Bots granted `can_post_messages` permissions can delete outgoing messages in channels.
 * - If the bot is an administrator of a group, it can delete any message there.
 * - If the bot has `can_delete_messages` permission in a supergroup or a channel, it can delete any message there.
 * @param chatId Unique identifier for the target chat.
 * @param messageId Identifier of the message to delete.
 * @param delay lifetime of the message to delete
 * @return True on success.
 */
fun Bot.delayDeleteMessage(chatId: Long, messageId: Long, delay: Duration = Duration.seconds(15)) =
  timer.schedule(delay.inWholeMilliseconds) {
    deleteMessage(ChatId.fromId(chatId), messageId)
  }


/**
 * The method will work correctly if periodic unpinning of obsolete messages is enabled.
 * @see Bot.scheduleUnpinMessages
 */
fun Bot.pinChatMessageTemporary(db: PinnedMessages, chat: Long, m: Long, duration: Duration) =
  db.access {
    val chatMap = it.getOrPut(chat, ::LinkedHashMap)
    chatMap[m] = duration.inWholeSeconds
    pinChatMessage(ChatId.fromId(chat), m)
  }

/**
 * The task of periodic unpinning obsolete messages
 */
fun Bot.scheduleUnpinMessages(db: PinnedMessages) =
  timer.schedule(period = 1_000, delay = 0) {
    val currentEpochSecond = Instant.now().epochSecond
    db.access { pins ->
      for ((chat, messages) in pins) {
        val chatId = ChatId.fromId(chat)
        val deprecatedMessages =
          messages
            .filterValues { it <= currentEpochSecond }
            .keys
        for (messageId in deprecatedMessages) {
          unpinChatMessage(chatId, messageId)
          messages.remove(messageId)
        }
      }
    }
  }

fun Bot.execute(query: String, message: Message, coins: Timecoins, pins: PinnedMessages) {
  val token = query.tokenize().iterator()
  execute(token, message, coins, pins)
}

fun Bot.execute(token: Iterator<Token>, message: Message, coins: Timecoins, pins: PinnedMessages) {
  if (!token.hasNext()) {
    log.warn("no token")
    return
  }
  when (val semnorm = token.next().semnorm) {
    is Executable -> {
      log.warn("Executable $semnorm")
      semnorm.execute(token, this, message, coins, pins)
    }
    else -> {
      log.warn("No executable: $semnorm")
    }
  }
}