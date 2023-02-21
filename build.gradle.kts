import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

fun properties(key: String) = project.findProperty(key).toString()

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("org.jetbrains.intellij") version "1.13.0"
    id("org.jetbrains.changelog") version "1.3.1"
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
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs += arrayOf("-opt-in=kotlin.RequiresOptIn")
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