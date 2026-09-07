package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.KattyUtils
import io.github.danbrough.katty.demoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.seconds


fun createFlow() = flow {
  var n = 0
  while(true) {
    n++
    demoLog.trace { "${KattyUtils.threadName()}: emitting item $n.." }
    emit("Item_$n")
  }
}.flowOn(KattyUtils.ioDispatcher).buffer(3)

suspend fun KTerminal.demoCoroutines(args: List<String>) {
  demoLog.info { "${KattyUtils.threadName()}: ${KattyUtils.threadName()}: demoCoroutines" }

  withContext(Dispatchers.Default) {

    val flow = createFlow()
    demoLog.info { "${KattyUtils.threadName()}: created flow: $flow" }
    delay(1.seconds)
    demoLog.info { "${KattyUtils.threadName()}: collecting on flow .." }
    flow.take(10).collect {
      println("${KattyUtils.threadName()}: $it")
      delay(1.seconds)
    }
    demoLog.info { "${KattyUtils.threadName()}: done collecting" }
  }

}


