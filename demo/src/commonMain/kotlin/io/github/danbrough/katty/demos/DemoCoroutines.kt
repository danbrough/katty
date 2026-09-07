package io.github.danbrough.katty.demos

import io.github.danbrough.katty.KTerminal
import io.github.danbrough.katty.KattyUtils
import kotlinx.coroutines.flow.flow


val myFlow = flow {
  for (n in 1..100) {
    emit("Item_$n")
  }
}

suspend fun KTerminal.demoCoroutines(args: List<String>) {
  println("${KattyUtils.threadName()}: demoCoroutines")

}


