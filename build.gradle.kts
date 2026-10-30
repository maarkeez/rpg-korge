import dependencyreport.DependencyReportTask
import korlibs.korge.gradle.*
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.testing.Test
import org.gradle.language.jvm.tasks.ProcessResources
import org.gradle.kotlin.dsl.register
import org.jmailen.gradle.kotlinter.tasks.ConfigurableKtLintTask
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import resourcesbundle.ResourcesBundleTask
import testcatalogs.TestCatalogsTask

// The korge plugin bundles kotlin-gradle-plugin 2.0.20 on its classpath; declaring the
// newer version here forces Gradle to resolve the conflict to 2.2.21 (highest version wins),
// so the kotlin-multiplatform and kotlinx-serialization plugins are applied from 2.2.21.
// Note: the JS compiler cannot link a newer stdlib than itself, so the Kotlin version must be at least
// the one the test libraries (e.g.: Kotest) are built with.
buildscript {
    repositories { mavenCentral() }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.21")
        classpath("org.jetbrains.kotlin:kotlin-serialization:2.2.21")
    }
    // kotlinter brings kotlin-compiler-embeddable 2.0.21 on the classpath, which bundles a
    // stale copy of org.jetbrains.kotlin.buildtools.api.CompilationService that shadows the
    // 2.2.21 one from kotlin-build-tools-api and breaks KGP's classpath snapshot transforms.
    configurations.classpath {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
        }
    }
}

plugins {
    alias(libs.plugins.korge)
    id("org.jmailen.kotlinter") version "4.5.0"
}

val pitest by configurations.creating

korge {
    id = "com.sample.demo"
    jvmMainClassName = "com.mkz.rpg.MainKt"

    // Adds `runJvmStorybook` and `runJvmStorybookAutoreload`, which start the Storybook instead of the game. See below.
    entrypoint("storybook", "com.mkz.rpg.MainKt")

    // To enable all targets at once
    // targetAll()

    // To enable targets based on properties/environment variables
    // targetDefault()

    // To selectively enable targets
    targetJvm()
    targetJs()
    //targetWasmJs()
    //targetIos()
    //targetAndroid()

    serializationJson()
}

dependencies {
    add("commonMainApi", project(":deps"))
    add("commonMainApi", "com.akuleshov7:ktoml-core:0.7.1")

    add("commonTestImplementation", "io.kotest:kotest-assertions-core:6.2.5")
    add("commonTestImplementation", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")

    add("jvmTestRuntimeOnly", "org.junit.jupiter:junit-jupiter:6.1.3")
    add("jvmTestApi", "com.tngtech.archunit:archunit:1.5.0")
    add("jvmTestApi", "com.lemonappdev:konsist:0.13.0")

    add("pitest", "org.pitest:pitest-command-line:1.25.8")
    add("pitest", "org.pitest:pitest-junit5-plugin:1.2.3")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

// The Storybook starts from the same `main` as the game, which reads this system property.
tasks.matching { it.name in setOf("runJvmStorybook", "runJvmStorybookAutoreload") }.configureEach {
    (this as JavaExec).systemProperty("rpg.entry", "storybook")
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

// The game reads its resources from a single bundle, so it loads them with one request instead of one per file.
// Contributors keep editing the files under `resources`, and only the bundle is packaged. See docs/architecture/assets.md.
val resourcesBundle =
    tasks.register<ResourcesBundleTask>("resourcesBundle") {
        group = "build"
        description = "Bundles the files under resources into the single file the game reads them from."

        sourceDirectory.set(file("resources"))
        bundleFileName.set("resources.zip")
        outputDirectory.set(layout.buildDirectory.dir("generated/resourcesBundle"))
    }

// The tests read the bundle too, as the JS tests are only served the resources of the test compilation.
extensions.getByType<KotlinMultiplatformExtension>().sourceSets.matching { it.name in setOf("commonMain", "commonTest") }.configureEach {
    resources.setSrcDirs(listOf(resourcesBundle.flatMap { it.outputDirectory }))
}

// The tests translate with the real catalogs, embedded in a generated source, as the JS tests can only read the
// resources asynchronously. See docs/architecture/localization.md.
val testCatalogs =
    tasks.register<TestCatalogsTask>("testCatalogs") {
        group = "build"
        description = "Generates the test source holding the localization catalogs."

        localizationDirectory.set(file("resources/localization"))
        packageName.set("com.mkz.rpg.localization.adapters.resources")
        objectName.set("TestCatalogs")
        outputDirectory.set(layout.buildDirectory.dir("generated/testCatalogs"))
    }

extensions.getByType<KotlinMultiplatformExtension>().sourceSets.named("commonTest") {
    kotlin.srcDir(testCatalogs.flatMap { it.outputDirectory })
}

// KorGE always packages the files under `resources`, with a `$catalog.json` listing each folder: both are left out.
tasks.withType<ProcessResources>().configureEach {
    val looseResourcesDirectory = file("resources")
    includeEmptyDirs = false
    eachFile {
        if (file.startsWith(looseResourcesDirectory) || name == "\$catalog.json") exclude()
    }
}

tasks.register<DependencyReportTask>("dependencyReport") {
    group = "documentation"
    description = "Generates the interactive report of the dependencies between the subdomains. See docs/architecture/dependencies.md."

    sourceDirectory.set(file("src/commonMain/kotlin"))
    basePackage.set("com.mkz.rpg")
    reportFile.set(file("docs/architecture/dependencies.html"))
}

tasks.register("allTargetTests") {
    group = "verification"
    description = "Runs tests for all enabled targets."

    dependsOn(
        tasks.named("jvmTest"),
        tasks.named("jsTest")
    )
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
        // A leading '~' makes PIT read the pattern as a regex. Only classes declared directly in a
        // `<subdomain>.adapters.presentation` package are excluded, so nested presentation subdomains
        // (e.g.: `screen.adapters.presentation.battlefieldHud`) are still mutation tested.
        // Note: PIT escapes '+' and the CLI splits on ',', so neither can be used in the regex.
        """--excludedClasses=~com\.mkz\.rpg\.[^.]*\.adapters\.presentation\.[^.]*""",
        "--excludedTestClasses=com.mkz.rpg.shared.adapters.architecture.*,com.mkz.rpg.shared.adapters.e2e.*",
        "--sourceDirs=${commonMainSources.absolutePath},${jvmMainSources.absolutePath}",
        "--mutableCodePaths=${jvmMainClasses.absolutePath}",
        "--outputFormats=HTML,XML",
        "--timestampedReports=false",
        "--threads=${Runtime.getRuntime().availableProcessors()}",
        "--failWhenNoMutations=true"
    )
}
