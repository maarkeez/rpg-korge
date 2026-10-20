package dependencyreport

// Renders the report by embedding the graph, as JSON, in the HTML template.
// The JSON is written one entry per line, so the diff of the report shows which dependencies changed.
object DependencyReportPage {
    private const val TEMPLATE = "/dependency-report.html"
    private const val DATA_PLACEHOLDER = "/*DEPENDENCY_DATA*/null"

    fun render(
        graph: DependencyGraph,
        sourceRoot: String,
    ): String {
        val template =
            checkNotNull(DependencyReportPage::class.java.getResource(TEMPLATE)) { "Can't find the template $TEMPLATE" }.readText()
        check(DATA_PLACEHOLDER in template) { "Can't find $DATA_PLACEHOLDER in the template $TEMPLATE" }
        return template.replace(DATA_PLACEHOLDER, json(graph, sourceRoot))
    }

    private fun json(
        graph: DependencyGraph,
        sourceRoot: String,
    ): String =
        buildString {
            appendLine("{")
            appendLine("""  "sourceRoot": ${sourceRoot.quoted()},""")
            appendLine("""  "subdomains": [""")
            appendEntries(graph.metrics) { metrics ->
                val instability = metrics.instability?.toString() ?: "null"
                append(
                    """    {"name": ${metrics.subdomain.quoted()}, "files": ${graph.fileCountBySubdomain.getValue(metrics.subdomain)}, """ +
                        """"fanIn": ${metrics.fanIn}, "fanOut": ${metrics.fanOut}, "instability": $instability, """ +
                        """"importsIn": ${metrics.importsIn}, "importsOut": ${metrics.importsOut}}""",
                )
            }
            appendLine("  ],")
            appendLine("""  "edges": [""")
            appendEntries(graph.edges) { edge ->
                appendLine(
                    """    {"from": ${edge.from.quoted()}, "to": ${edge.to.quoted()}, "weight": ${edge.weight}, "files": ${edge.fileCount}, "imports": [""",
                )
                appendEntries(edge.imports) { import ->
                    append("""      {"file": ${import.sourceFile.quoted()}, "layer": ${import.sourceLayer.quoted()}, "name": ${import.importedName.quoted()}}""")
                }
                append("    ]}")
            }
            appendLine("  ],")
            appendLine("""  "cycles": [""")
            appendEntries(graph.cycles) { cycle -> append("    [${cycle.joinToString { it.quoted() }}]") }
            appendLine("  ],")
            appendLine("""  "mutualDependencies": [""")
            appendEntries(graph.mutualDependencies) { (first, second) -> append("    [${first.quoted()}, ${second.quoted()}]") }
            appendLine("  ]")
            append("}")
        }

    private fun <T> StringBuilder.appendEntries(
        entries: List<T>,
        appendEntry: StringBuilder.(T) -> Unit,
    ) {
        entries.forEachIndexed { index, entry ->
            appendEntry(entry)
            appendLine(if (index < entries.lastIndex) "," else "")
        }
    }

    // Escapes `<` too, so a `</script>` in a name can not close the script that embeds the JSON.
    private fun String.quoted(): String =
        buildString {
            append('"')
            this@quoted.forEach { char ->
                when (char) {
                    '"' -> append("\\\"")
                    '\\' -> append("\\\\")
                    '<' -> append("\\u003c")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> if (char < ' ') append("\\u%04x".format(char.code)) else append(char)
                }
            }
            append('"')
        }
}
