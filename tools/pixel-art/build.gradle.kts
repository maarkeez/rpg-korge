// A standalone build, not included by the game's.

// As in the game's build: kotlinter brings kotlin-compiler-embeddable 2.0.21, whose stale copy of the build tools API
// breaks the classpath snapshots of the Kotlin 2.2.21 plugin.
buildscript {
    configurations.classpath {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
        }
    }
}

plugins {
    kotlin("jvm") version "2.2.21"
    id("org.jmailen.kotlinter") version "4.5.0"
    application
}

kotlin {
    jvmToolchain(21)
}

application {
    applicationName = "pixel"
    mainClass.set("com.mkz.pixelart.MainKt")
    // The previews are drawn with java.awt, without a display.
    applicationDefaultJvmArgs = listOf("-Djava.awt.headless=true", "-XX:TieredStopAtLevel=1", "-Xshare:auto")
}

dependencies {
    implementation("com.github.ajalt.clikt:clikt:5.0.3")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:6.2.5")
    // KorGE's Aseprite reader, an independent implementation of the format, decodes the files the tool writes.
    testImplementation("com.soywiz:korlibs-image:6.0.0")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("java.awt.headless", "true")
    // The tests compare the tool's palette with the game's.
    systemProperty("pixelart.resources", rootDir.resolve("../../resources").absolutePath)
}
