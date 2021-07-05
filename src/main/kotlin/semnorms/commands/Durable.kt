package semnorms.commands

import Query
import com.github.demidko.tokenizer.Token
import semnorms.Executable
import semnorms.Minute.toDuration
import semnorms.Rule
import semnorms.Semnorm
import semnorms.Time
import kotlin.time.Duration

abstract class Durable(vararg rules: Rule) : Executable(*rules) {

  final override fun execute(query: Query) = execute(query, query.token.parseDuration())

  abstract fun execute(query: Query, duration: Duration)
}

fun Iterator<Token<Semnorm?>>.parseDuration(): Duration {
  val (token, norm) = next()
  return when (norm) {
    is Time -> try {
      norm.toDuration(parseLong())
    } catch (ignored: RuntimeException) {
      norm.toDuration(1)
    }
    is semnorms.Number -> try {
      parseTime().toDuration(token.toLong())
    } catch (ignored: RuntimeException) {
      toDuration(token.toLong())
    }
    else -> parseDuration()
  }
}

fun Iterator<Token<Semnorm?>>.parseTime(): Time {
  val (_, norm) = next()
  return when (norm) {
    is Time -> norm
    else -> parseTime()
  }
}

fun Iterator<Token<Semnorm?>>.parseLong(): Long {
  val (token, norm) = next()
  return when (norm) {
    is Number -> token.toLong()
    else -> parseLong()
  }
}