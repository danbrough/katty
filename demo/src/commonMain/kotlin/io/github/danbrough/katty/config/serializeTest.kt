package io.github.danbrough.katty.config

import io.github.danbrough.katty.KTerminal


suspend fun KTerminal.serializeTest(args: List<String>) {
  println("Serialize Test: ${args.joinToString(",")}")
  
}