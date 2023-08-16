import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    alias(libs.plugins.kotlin) // Kotlin support
    alias(libs.plugins.kotlinSerialization) // Kotlin serialization support
    alias(libs.plugins.gradleIntelliJPlugin) // Gradle IntelliJ Plugin
    alias(libs.plugins.changelog) // Gradle Changelog Plugin
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.1")
}

kotlin {
    jvmToolchain(17)
}

group = properties("pluginGroup")
version = properties("pluginVersion")

repositories {
    mavenCentral()
}

intellij {
    pluginName.set(properties("pluginName"))
    version.set(properties("platformVersion"))
    type.set(properties("platformType"))
    plugins.set(properties("platformPlugins").split(',').map(String::trim).filter(String::isNotEmpty))
}

changelog {
    version.set(properties("pluginVersion"))
    groups.set(emptyList())
}

tasks {
    wrapper {
        gradleVersion = properties("gradleVersion")
    }

    patchPluginXml {
        version.set(properties("pluginVersion"))
        sinceBuild.set(properties("pluginSinceBuild"))
        untilBuild.set(properties("pluginUntilBuild"))

        pluginDescription.set(
            projectDir.resolve("README.md").readText().lines().run {
                val start = "<!-- Plugin description -->"
                val end = "<!-- Plugin description end -->"
                if (!containsAll(listOf(start, end))) {
                    throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
                }
                subList(indexOf(start) + 1, indexOf(end))
            }.joinToString("\n").run { markdownToHTML(this) }
        )

        changeNotes.set(provider {
            changelog.run {
                getOrNull(properties("pluginVersion")) ?: getLatest()
            }.toHTML()
        })
    }

    runIde {
        jvmArgs = listOf("-Xmx2g")
        systemProperty("idea.disposer.debug", "on")
        systemProperty("idea.log.debug.categories", "#org.yh.statistics")
    }

    signPlugin {
        certificateChain.set(getenv("CERTIFICATE_CHAIN"))
        privateKey.set(getenv("PRIVATE_KEY"))
        password.set(getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(getenv("PUBLISH_TOKEN"))
    }
}