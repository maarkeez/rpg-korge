package testcatalogs

import java.io.File

// Writes the Kotlin source of an object holding the content of every catalog of a localization folder, keyed by file
// name (e.g.: `messages.pot`, `es.po`), so the tests translate with the real catalogs without reading the resources,
// which the JS tests can only do asynchronously. One string per line, as a single string constant may be too long.
object TestCatalogs {
    private val CATALOG_EXTENSIONS = setOf("pot", "po")

    fun source(
        localizationRoot: File,
        packageName: String,
        objectName: String,
    ): String {
        val catalogs =
            localizationRoot
                .listFiles()
                .orEmpty()
                .filter { it.isFile && it.extension in CATALOG_EXTENSIONS }
                .sortedBy(File::getName)
        return buildString {
            appendLine("// Generated from ${localizationRoot.name}/ by the testCatalogs task. Do not edit.")
            appendLine("package $packageName")
            appendLine()
            appendLine("object $objectName {")
            appendLine("    val files: Map<String, String> =")
            appendLine("        mapOf(")
            catalogs.forEach { catalog ->
                appendLine("            ${quote(catalog.name)} to")
                appendLine("                listOf(")
                catalog.readText().lines().forEach { line -> appendLine("                    ${quote(line)},") }
                appendLine("                ).joinToString(\"\\n\"),")
            }
            appendLine("        )")
            appendLine("}")
        }
    }

    private fun quote(text: String): String =
        buildString {
            append('"')
            text.forEach { character ->
                when (character) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '$' -> append("\\$")
                    '\t' -> append("\\t")
                    '\r' -> append("\\r")
                    else -> append(character)
                }
            }
            append('"')
        }
}
