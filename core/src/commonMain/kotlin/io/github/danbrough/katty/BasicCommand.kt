package io.github.danbrough.katty

open class BasicCommand(
  val name: String,
  val description: String,
  val job: (KTerminal.(args: List<String>) -> Unit)? = null
) : CommandHandler {
  override fun matches(args: List<String>): Boolean = args.firstOrNull() == name
  override fun exec(kTerminal: KTerminal, args: List<String>) =
    job?.invoke(kTerminal, args) ?: error("Exec for $name not implemented")

  override fun helpText(): String = "$name: $description"

}
