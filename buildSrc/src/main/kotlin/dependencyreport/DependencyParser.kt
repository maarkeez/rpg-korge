package dependencyreport

data class KotlinSourceFile(
    val path: String,
    val content: String,
)

// One `import` of a subdomain from a file of another subdomain. Names are relative to the base package.
data class ImportDependency(
    val sourceFile: String,
    val sourceSubdomain: String,
    val sourceLayer: String,
    val targetSubdomain: String,
    val importedName: String,
)

// Reads the subdomain dependencies from the `import` lines, so it needs no compilation.
// The subdomain of a file is the first package segment after the base package, so a subdomain nested in another one
// (e.g.: `screen.adapters.presentation.battlefieldHud`) is part of it. Files declared in the base package (e.g.: `Main.kt`) are ignored.
class DependencyParser(
    private val basePackage: String,
) {
    fun parse(files: List<KotlinSourceFile>): DependencyGraph {
        val subdomainFiles =
            files.mapNotNull { file ->
                val relativePackage = file.relativePackage() ?: return@mapNotNull null
                ParsedFile(file, relativePackage.split("."))
            }
        val subdomains = subdomainFiles.map { it.subdomain }.toSet()
        val dependencies =
            subdomainFiles.flatMap { parsed ->
                parsed.file.imports()
                    .mapNotNull { it.relativeToBasePackage() }
                    .filter { importedName ->
                        val targetSubdomain = importedName.substringBefore(".")
                        targetSubdomain in subdomains && targetSubdomain != parsed.subdomain
                    }.map { importedName ->
                        ImportDependency(
                            sourceFile = parsed.file.path,
                            sourceSubdomain = parsed.subdomain,
                            sourceLayer = parsed.layer,
                            targetSubdomain = importedName.substringBefore("."),
                            importedName = importedName,
                        )
                    }
            }
        return DependencyGraph(
            fileCountBySubdomain = subdomainFiles.groupingBy { it.subdomain }.eachCount(),
            dependencies = dependencies,
        )
    }

    private fun KotlinSourceFile.relativePackage(): String? {
        val packageName = PACKAGE_LINE.find(content)?.groupValues?.get(1) ?: return null
        return packageName.relativeToBasePackage()
    }

    private fun KotlinSourceFile.imports(): List<String> = IMPORT_LINE.findAll(content).map { it.groupValues[1] }.toList()

    private fun String.relativeToBasePackage(): String? = removePrefix("$basePackage.").takeIf { it != this }

    private class ParsedFile(
        val file: KotlinSourceFile,
        packageSegments: List<String>,
    ) {
        val subdomain = packageSegments.first()
        val layer = packageSegments.getOrElse(1) { "" }
    }

    private companion object {
        val PACKAGE_LINE = Regex("""^package\s+([\w.]+)""", RegexOption.MULTILINE)
        val IMPORT_LINE = Regex("""^import\s+([\w.]+)""", RegexOption.MULTILINE)
    }
}
