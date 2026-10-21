package resourcesbundle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// Bundles the game resources into the single file the game reads them from. See docs/architecture/assets.md.
abstract class ResourcesBundleTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val bundleFileName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputRoot = outputDirectory.get().asFile
        outputRoot.deleteRecursively()
        outputRoot.mkdirs()
        val bundle = outputRoot.resolve(bundleFileName.get())
        bundle.outputStream().use { output -> ResourcesBundle.write(sourceDirectory.get().asFile, output) }
        logger.info("Resources bundle written to $bundle: ${bundle.length()} bytes")
    }
}
