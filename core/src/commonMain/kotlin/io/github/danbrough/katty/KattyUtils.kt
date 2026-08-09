package io.github.danbrough.katty

interface Utils {
  fun getEnv(name: String): String?
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object KattyUtils : Utils {
  override fun getEnv(name: String): String?

}


