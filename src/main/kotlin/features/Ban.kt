package features

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatPermissions
import com.github.kotlintelegrambot.entities.Message
import org.slf4j.LoggerFactory.getLogger
import storages.TimeBank
import telegram.delayDeleteMessage
import telegram.sendTempMessage
import java.time.Instant
import java.time.Instant.now
import java.time.Instant.ofEpochSecond
import java.time.LocalDateTime.now
import java.time.ZoneId.of
import kotlin.time.Duration
import kotlin.time.seconds

private val customBan = ChatPermissions(
  canSendMessages = false,
  canSendMediaMessages = false,
  canSendPolls = false,
  canSendOtherMessages = false,
  canAddWebPagePreviews = false,
  canChangeInfo = false,
  canInviteUsers = true,
  canPinMessages = false,
)

private val VLAT = of("Asia/Vladivostok")

private val log = getLogger("Ban")

fun Bot.ban(duration: Duration, attackerMessage: Message, storage: TimeBank) {

  @Suppress("NAME_SHADOWING")
  val duration = if (duration < 30.seconds) {
    sendTempMessage(
      attackerMessage.chat.id,
      "$duration is too small for telegram api, 30 seconds are used.",
      replyToMessageId = attackerMessage.messageId,
      lifetime = 3.seconds
    )
    30.seconds
  } else {
    duration
  }

  val attacker = attackerMessage
    .from
    ?.id
    ?: error("You hasn't telegram id")
  val victimMessage = attackerMessage
    .replyToMessage
    ?: error("You need to reply to the user to ban him")
  val victim = victimMessage
    .from
    ?.id
    ?: error("You need to reply to the user with telegram id to ban him")
  storage.use(attacker, duration) {

    val untilSecond = Instant.now().epochSecond + it.inSeconds.toLong()

    log.warn(
      "${attackerMessage.text} restricted ${victimMessage.text} until ${
        ofEpochSecond(
          untilSecond
        ).atZone(VLAT).toLocalDateTime()
      }"
    )

    restrictChatMember(
      attackerMessage.chat.id,
      victim,
      customBan,
      untilSecond
    )
    sendTempMessage(
      attackerMessage.chat.id,
      "💥",
      replyToMessageId = victimMessage.messageId,
    )
  }
  delayDeleteMessage(attackerMessage.chat.id, attackerMessage.messageId)
}