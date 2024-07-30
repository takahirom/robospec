plugins {
    alias(libs.plugins.jetbrains.kotlin.multiplatform)
    id("module.publication")
}

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(libs.coroutines)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.coroutines)
                implementation(libs.junit)
            }
        }
    }

}