// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  id("root.publication")
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
}