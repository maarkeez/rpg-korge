plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    // Kotest 6 is built with a newer Kotlin than the one embedded in Gradle, which compiles buildSrc.
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
}

tasks.test {
    useJUnitPlatform()
}
