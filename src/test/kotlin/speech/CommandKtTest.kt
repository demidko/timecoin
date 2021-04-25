package speech

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test
import kotlin.time.hours
import kotlin.time.minutes

class CommandKtTest {

  @Test
  fun toCommand() {
    val speech = "забань васю на 5 минут".command()
    assertThat(speech, equalTo(BanCommand(5.minutes)))
  }

  @Test
  fun toCommandOtherCase() {
    val speech = "переведи часов наверное 17 Пете".command()
    assertThat(speech, equalTo(TransferCommand(17.hours)))
  }

  @Test
  fun checkStatus() {
    assertThat("счет".command(), equalTo(StatusCommand))
  }
}