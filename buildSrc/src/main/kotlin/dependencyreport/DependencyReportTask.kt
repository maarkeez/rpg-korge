package dependencyreport

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

// Generates the interactive HTML report of the dependencies between the subdomains. See docs/architecture/dependencies.md.
abstract class DependencyReportTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectory: DirectoryProperty

    @get:Input
    abstract val basePackage: Property<String>

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @TaskAction
    fun generate() {
        val sourceRoot = sourceDirectory.get().asFile
        val report = reportFile.get().asFile
        val files =
            sourceRoot
                .walkTopDown()
                .filter { it.isFile && it.extension == "kt" }
                .map { KotlinSourceFile(path = it.relativeTo(sourceRoot).invariantSeparatorsPath, content = it.readText()) }
                .sortedBy { it.path }
                .toList()
        val graph = DependencyParser(basePackage.get()).parse(files)
        // The report links each file from its own folder, so the links work wherever the repository is checked out.
        val sourceRootFromReport = sourceRoot.relativeTo(report.parentFile).invariantSeparatorsPath
        report.parentFile.mkdirs()
        report.writeText(DependencyReportPage.render(graph, sourceRootFromReport))
        logger.info("Dependency report written to $report: ${graph.subdomains.size} subdomains, ${graph.edges.size} dependencies, ${graph.cycles.size} cycles")
    }
}
