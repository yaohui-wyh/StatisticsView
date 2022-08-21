import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.lang.System.getenv

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
    id("org.jetbrains.intellij") version "1.8.0"
}

group = "org.yh.statistics"
version = "1.0"

repositories {
    mavenCentral()
}

intellij {
    version.set("2021.2")
    type.set("IC") // Target IDE Platform
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "11"
        targetCompatibility = "11"
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        kotlinOptions.freeCompilerArgs += arrayOf("-opt-in=kotlin.RequiresOptIn")
    }

    patchPluginXml {
        sinceBuild.set("212")
        untilBuild.set("222.*")
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
