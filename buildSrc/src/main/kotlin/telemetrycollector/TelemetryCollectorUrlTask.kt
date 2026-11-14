package telemetrycollector

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

// Generates the source holding the address of the telemetry collector the game sends its product events to, or null,
// when no address is given, so the game sends nothing.
abstract class TelemetryCollectorUrlTask : DefaultTask() {
    @get:Input
    @get:Optional
    abstract val collectorUrl: Property<String>

    @get:Input
    abstract val packageName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val url = collectorUrl.orNull?.trimEnd('/')
        require(url == null || Regex("https?://[A-Za-z0-9.:-]+").matches(url)) { "tessavar.telemetryCollectorUrl is not an http(s) address without a path: $url" }
        val outputRoot = outputDirectory.get().asFile
        outputRoot.deleteRecursively()
        val source = outputRoot.resolve(packageName.get().replace('.', '/')).resolve("TelemetryCollectorUrl.kt")
        source.parentFile.mkdirs()
        source.writeText(
            buildString {
                appendLine("// Generated from the Gradle property tessavar.telemetryCollectorUrl by the telemetryCollectorUrl task. Do not edit.")
                appendLine("package ${packageName.get()}")
                appendLine()
                appendLine("val TELEMETRY_COLLECTOR_URL: String? = ${if (url == null) "null" else "\"$url\""}")
            },
        )
    }
}
