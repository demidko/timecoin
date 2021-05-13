package features

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

internal class BalanceKtTest {

  @Test
  fun toHumanTime() {
    assertThat(0L.secondsToHumanTime(), equalTo("You don't have time"))
    assertThat((60L * 60L * 24L * 30L * 12L).secondsToHumanTime(), equalTo("You have 1 year"))
    assertThat((60L * 60L * 24L * 30L * 12L + 12).secondsToHumanTime(), equalTo("You have 1 year 12s"))
    assertThat(1232443224L.secondsToHumanTime(), equalTo("You have 39 years 7 months 14d 9h 20m 24s"))
  }
}