object Thang {
  const val MESSAGE = "Thang message"
}

tasks.register("thang") {
  description = "Prints the thang"
  actions.add {
    println("The message is ${Thang.MESSAGE}")
  }
}