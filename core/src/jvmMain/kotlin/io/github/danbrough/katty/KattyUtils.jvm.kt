package io.github.danbrough.katty

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object KattyUtils : Utils {
  actual override fun getEnv(name: String): String? = System.getenv(name)
}