import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.`maven-publish`

plugins {
    `maven-publish`
    signing
}

publishing {
    // Configure all publications
    publications.withType<MavenPublication> {
        // Stub javadoc.jar artifact
        artifact(tasks.register("${name}JavadocJar", Jar::class) {
            archiveClassifier.set("javadoc")
            archiveAppendix.set(this@withType.name)
        })

        // Provide artifacts information required by Maven Central
        pom {
            name.set("RoboSpec")
            description.set("RoboSpec is a library that provides a way to write tests using describe and itShould blocks with Robolectric.")
            url.set("https://github.com/takahirom/robospec")

            licenses {
                license {
                    name.set("Apache License 2.0")
                    url.set("https://opensource.org/licenses/Apache-2.0.html")
                }
            }
            developers {
                developer {
                    id.set("takahirom")
                    name.set("takahirom")
                }
            }
            scm {
                url.set("https://github.com/takahirom/robospec")
            }
        }
    }
}

signing {
    if (project.hasProperty("signing.gnupg.keyName")) {
        useGpgCmd()
        sign(publishing.publications)
    }
}
