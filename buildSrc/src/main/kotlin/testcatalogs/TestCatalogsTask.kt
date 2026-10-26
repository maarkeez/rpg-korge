package testcatalogs

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// Generates the test source holding the localization catalogs. See docs/architecture/localization.md.
abstract class TestCatalogsTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val localizationDirectory: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val objectName: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun generate() {
        val outputRoot = outputDirectory.get().asFile
        outputRoot.deleteRecursively()
        val source = outputRoot.resolve(packageName.get().replace('.', '/')).resolve("${objectName.get()}.kt")
        source.parentFile.mkdirs()
        source.writeText(TestCatalogs.source(localizationDirectory.get().asFile, packageName.get(), objectName.get()))
    }
}
