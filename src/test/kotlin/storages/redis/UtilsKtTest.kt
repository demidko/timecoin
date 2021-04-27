package storages.redis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.Test

internal class UtilsKtTest {

  @Test
  fun redisClient() =
    redisClient("rediss://root:23449cdf4934fd@crack-do-user-randombd.jb.ondigitalocean.com:37081")
      .useSingleServer()
      .run {
        assertThat(username, equalTo("root"))
        assertThat(password, equalTo("23449cdf4934fd"))
      }
}