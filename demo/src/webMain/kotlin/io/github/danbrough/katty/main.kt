package io.github.danbrough.katty

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@OptIn(DelicateCoroutinesApi::class)
fun main(args: Array<String>) {
  println("running node demo .. args: ${args.joinToString()}")
  GlobalScope.launch {
    demoMain(args)
  }
}