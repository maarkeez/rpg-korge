package dependencyreport

import kotlin.math.roundToInt

// Every list is sorted, so the report only changes when the dependencies do.
class DependencyGraph(
    fileCountBySubdomain: Map<String, Int>,
    dependencies: List<ImportDependency>,
) {
    val subdomains: List<String> = fileCountBySubdomain.keys.sorted()

    val fileCountBySubdomain: Map<String, Int> = fileCountBySubdomain.toSortedMap()

    val edges: List<Edge> =
        dependencies
            .groupBy { it.sourceSubdomain to it.targetSubdomain }
            .map { (subdomains, imports) ->
                Edge(
                    from = subdomains.first,
                    to = subdomains.second,
                    imports = imports.sortedWith(compareBy({ it.sourceFile }, { it.importedName })),
                )
            }.sortedWith(compareBy({ it.from }, { it.to }))

    val metrics: List<SubdomainMetrics> =
        subdomains.map { subdomain ->
            val incoming = edges.filter { it.to == subdomain }
            val outgoing = edges.filter { it.from == subdomain }
            SubdomainMetrics(
                subdomain = subdomain,
                fanIn = incoming.size,
                fanOut = outgoing.size,
                importsIn = incoming.sumOf { it.weight },
                importsOut = outgoing.sumOf { it.weight },
            )
        }

    // The groups of subdomains that can reach each other (strongly connected components), with more than one subdomain.
    val cycles: List<List<String>> =
        stronglyConnectedComponents()
            .filter { it.size > 1 }
            .map { it.sorted() }
            .sortedBy { it.first() }

    // The pairs of subdomains that depend directly on each other: the shortest cycles.
    val mutualDependencies: List<Pair<String, String>> =
        edges
            .filter { it.from < it.to && edges.any { other -> other.from == it.to && other.to == it.from } }
            .map { it.from to it.to }

    private fun stronglyConnectedComponents(): List<List<String>> {
        val successors = subdomains.associateWith { subdomain -> edges.filter { it.from == subdomain }.map { it.to } }
        val tarjan = Tarjan(successors)
        subdomains.forEach(tarjan::visit)
        return tarjan.components
    }

    private class Tarjan(
        private val successors: Map<String, List<String>>,
    ) {
        val components = mutableListOf<List<String>>()
        private val indexes = mutableMapOf<String, Int>()
        private val lowLinks = mutableMapOf<String, Int>()
        private val stack = ArrayDeque<String>()

        fun visit(node: String) {
            if (node in indexes) return
            indexes[node] = indexes.size
            lowLinks[node] = indexes.getValue(node)
            stack.addLast(node)
            successors.getValue(node).forEach { successor ->
                if (successor !in indexes) {
                    visit(successor)
                    lowLinks[node] = minOf(lowLinks.getValue(node), lowLinks.getValue(successor))
                } else if (successor in stack) {
                    lowLinks[node] = minOf(lowLinks.getValue(node), indexes.getValue(successor))
                }
            }
            if (lowLinks[node] == indexes[node]) {
                val component = mutableListOf<String>()
                do {
                    val member = stack.removeLast()
                    component += member
                } while (member != node)
                components += component
            }
        }
    }
}

data class Edge(
    val from: String,
    val to: String,
    val imports: List<ImportDependency>,
) {
    val weight: Int = imports.size

    val fileCount: Int = imports.map { it.sourceFile }.distinct().size
}

data class SubdomainMetrics(
    val subdomain: String,
    // Distinct subdomains that depend on this one.
    val fanIn: Int,
    // Distinct subdomains this one depends on.
    val fanOut: Int,
    val importsIn: Int,
    val importsOut: Int,
) {
    // Robert C. Martin's instability, fanOut / (fanIn + fanOut), from 0 (stable) to 1 (unstable). Undefined for a subdomain with no dependencies.
    val instability: Double? =
        if (fanIn + fanOut == 0) null else (fanOut * 100.0 / (fanIn + fanOut)).roundToInt() / 100.0
}
