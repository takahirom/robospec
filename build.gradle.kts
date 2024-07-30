import com.android.build.gradle.BaseExtension

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("root.publication")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.multiplatform) apply false
}

allprojects {
    val javaVersion = rootProject.libs.versions.javaTarget.get()
    val toolchainVersion = rootProject.libs.versions.javaToolchain.get()

    val javaTargetVersion = JavaVersion.toVersion(javaVersion)
    val jvmTargetVersion = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion)

    plugins.withId("java") {
        the<JavaPluginExtension>()
            .toolchain {
                languageVersion.set(JavaLanguageVersion.of(toolchainVersion))
            }
    }

    plugins.withType<com.android.build.gradle.BasePlugin>().configureEach {
        the<BaseExtension>()
            .compileOptions {
                sourceCompatibility = javaTargetVersion
                targetCompatibility = javaTargetVersion
            }
    }

    tasks.withType<JavaCompile>().configureEach {
        sourceCompatibility = javaTargetVersion.name
        targetCompatibility = javaTargetVersion.name
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(jvmTargetVersion)
        }
    }
}