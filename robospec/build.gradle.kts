plugins {
  alias(libs.plugins.jetbrains.kotlin.jvm)
}

dependencies {
  compileOnly(libs.coroutines)
  testImplementation(libs.coroutines)
  testImplementation(libs.junit)
}