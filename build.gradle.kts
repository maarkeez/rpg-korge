import korlibs.korge.gradle.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.register
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask

plugins {
    alias(libs.plugins.korge)
    id("org.jmailen.kotlinter") version "4.5.0"
}

val pitest by configurations.creating

korge {
    id = "com.sample.demo"
    jvmMainClassName = "com.mkz.rpg.MainKt"

    // To enable all targets at once
    // targetAll()

    // To enable targets based on properties/environment variables
    // targetDefault()

    // To selectively enable targets
    targetJvm()
    targetJs()
    targetWasm()
    targetDesktop()
    targetIos()
    targetAndroid()

    serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))

    add("jvmTestApi", "org.mockito.kotlin:mockito-kotlin:6.3.0")
    add("jvmTestApi", "org.assertj:assertj-core:3.27.7")
    add("jvmTestApi", "org.junit.jupiter:junit-jupiter:6.1.3")
    add("jvmTestApi", "com.tngtech.archunit:archunit:1.5.0")
    add("jvmTestApi", "com.lemonappdev:konsist:0.13.0")

    add("pitest", "org.pitest:pitest-command-line:1.25.8")
    add("pitest", "org.pitest:pitest-junit5-plugin:1.2.3")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

tasks.withType<ConfigurableKtLintTask>().configureEach {
    doFirst {
        val buildDirAbs = project.layout.buildDirectory.get().asFile.absoluteFile
        val all = source.files.filter { it.isFile }
        val filtered = all.filter { !it.absoluteFile.startsWith(buildDirAbs) }

        if (filtered.size != all.size) {
            setSource(filtered)
        }
    }
}

val pitestReport = tasks.register<JavaExec>("pitest") {
    group = "verification"
    description = "Runs PIT mutation testing against the JVM test suite."

    dependsOn("jvmTest")

    mainClass.set("org.pitest.mutationtest.commandline.MutationCoverageReport")

    classpath = pitest

    val reportDir = layout.buildDirectory
        .dir("reports/pitest")
        .get()
        .asFile

    val commonMainSources = file("src/commonMain/kotlin")
    val jvmMainSources = file("src/jvmMain/kotlin")

    val jvmMainClasses = layout.buildDirectory
        .dir("classes/kotlin/jvm/main")
        .get()
        .asFile

    val jvmTestClasses = layout.buildDirectory
        .dir("classes/kotlin/jvm/test")
        .get()
        .asFile

    val jvmTestRuntimeClasspath: FileCollection = files(configurations.named("jvmTestRuntimeClasspath"))

    doFirst {
        if (!jvmMainClasses.exists()) {
            throw GradleException(
                "JVM main classes were not found at $jvmMainClasses. " +
                    "Make sure the JVM target is enabled and compilation succeeds."
            )
        }

        if (!jvmTestClasses.exists()) {
            throw GradleException(
                "JVM test classes were not found at $jvmTestClasses. " +
                    "Make sure jvmTest compiles successfully."
            )
        }

        val classPath = (listOf(jvmTestClasses) + jvmTestRuntimeClasspath.files)
            .distinct()
            .joinToString(",") { it.absolutePath }
        args("--classPath=$classPath")
    }

    args(
        "--reportDir=$reportDir",
        "--targetClasses=com.mkz.rpg.*",
        "--targetTests=com.mkz.rpg.*",
        "--excludedClasses=com.mkz.rpg.*.adapters.presentation.*",
        "--excludedTestClasses=com.mkz.rpg.shared.adapters.architecture.*",
        "--sourceDirs=${commonMainSources.absolutePath},${jvmMainSources.absolutePath}",
        "--mutableCodePaths=${jvmMainClasses.absolutePath}",
        "--outputFormats=HTML,XML",
        "--timestampedReports=false",
        "--threads=${Runtime.getRuntime().availableProcessors()}",
        "--failWhenNoMutations=true"
    )
}
