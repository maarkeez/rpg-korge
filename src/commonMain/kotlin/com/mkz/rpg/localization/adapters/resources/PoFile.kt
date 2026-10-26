package com.mkz.rpg.localization.adapters.resources

/**
 * A gettext PO file, or its POT template: its header and its entries, see
 * https://www.gnu.org/software/gettext/manual/html_node/PO-Files.html. Comments are skipped, but the flags (e.g.:
 * `#, fuzzy`), and so are obsolete entries (`#~`).
 */
data class PoFile(
    val header: Map<String, String>,
    val entries: List<Entry>,
) {
    /** An entry: its context (the key of the message), its English text, and its translation, one per plural form. */
    data class Entry(
        val context: String?,
        val id: String,
        val idPlural: String?,
        val translations: List<String>,
        val flags: Set<String>,
    ) {
        /** Whether it is translated: every form is, and it is not waiting for a review (fuzzy). */
        val isTranslated: Boolean
            get() = translations.isNotEmpty() && translations.all(String::isNotEmpty) && FUZZY !in flags
    }

    companion object {
        private const val FUZZY = "fuzzy"
        private val KEYWORD = Regex("""^(msgctxt|msgid_plural|msgid|msgstr(?:\[(\d+)\])?)\s+(".*)$""")

        /** Parses the [content] of a PO file, failing with the line number when it is not valid. */
        fun parse(content: String): PoFile {
            val entries = mutableListOf<Entry>()
            var entry = EntryBuilder()
            content.removePrefix("\uFEFF").lines().forEachIndexed { index, rawLine ->
                val lineNumber = index + 1
                val line = rawLine.trim()
                try {
                    when {
                        line.isEmpty() || line.startsWith("#~") -> Unit
                        line.startsWith("#,") -> {
                            if (entry.hasTranslations) entry = entry.finishInto(entries)
                            entry.flags +=
                                line
                                    .removePrefix("#,")
                                    .split(",")
                                    .map(String::trim)
                                    .filter(String::isNotEmpty)
                        }
                        line.startsWith("#") -> if (entry.hasTranslations) entry = entry.finishInto(entries)
                        line.startsWith("\"") -> entry.append(parseString(line))
                        else -> {
                            val match = KEYWORD.matchEntire(line) ?: throw IllegalArgumentException("Unexpected '$line'")
                            val (keyword, pluralIndex, value) = match.destructured
                            if (entry.hasTranslations && (keyword == "msgctxt" || keyword == "msgid")) entry = entry.finishInto(entries)
                            entry.set(keyword.substringBefore("["), pluralIndex.toIntOrNull(), parseString(value))
                        }
                    }
                } catch (exception: IllegalArgumentException) {
                    throw IllegalArgumentException("Line $lineNumber: ${exception.message}", exception)
                }
            }
            try {
                if (entry.hasStarted) entry.finishInto(entries)
            } catch (exception: IllegalArgumentException) {
                throw IllegalArgumentException("End of file: ${exception.message}", exception)
            }
            val header = entries.firstOrNull { it.context == null && it.id.isEmpty() }
            return PoFile(header = header?.let(::parseHeader).orEmpty(), entries = entries - setOfNotNull(header))
        }

        // A quoted string, with the escapes `\\`, `\"`, `\n`, `\t` and `\r`.
        private fun parseString(quoted: String): String {
            require(quoted.length >= 2 && quoted.startsWith("\"") && quoted.endsWith("\"")) { "Unterminated string $quoted" }
            val text = quoted.substring(1, quoted.length - 1)
            return buildString {
                var index = 0
                while (index < text.length) {
                    val character = text[index]
                    when {
                        character == '\\' -> {
                            index++
                            append(
                                when (text.getOrNull(index)) {
                                    '\\' -> '\\'
                                    '"' -> '"'
                                    'n' -> '\n'
                                    't' -> '\t'
                                    'r' -> '\r'
                                    else -> throw IllegalArgumentException("Unknown escape in $quoted")
                                },
                            )
                        }
                        character == '"' -> throw IllegalArgumentException("Unescaped quote in $quoted")
                        else -> append(character)
                    }
                    index++
                }
            }
        }

        // The header is the translation of the empty message: one `Name: value` field per line.
        private fun parseHeader(header: Entry): Map<String, String> =
            header.translations
                .firstOrNull()
                .orEmpty()
                .lines()
                .filter { line -> ":" in line }
                .associate { line -> line.substringBefore(":").trim() to line.substringAfter(":").trim() }
    }

    private class EntryBuilder {
        val flags = mutableSetOf<String>()
        private var context: String? = null
        private var id: String? = null
        private var idPlural: String? = null
        private val translations = mutableListOf<String>()

        // Appends a continuation line to the last field set.
        private var append: ((String) -> Unit)? = null

        val hasStarted: Boolean get() = flags.isNotEmpty() || context != null || id != null
        val hasTranslations: Boolean get() = translations.isNotEmpty()

        fun set(
            keyword: String,
            pluralIndex: Int?,
            value: String,
        ) {
            when (keyword) {
                "msgctxt" -> {
                    require(context == null && id == null) { "Unexpected msgctxt" }
                    context = value
                    append = { context = context.orEmpty() + it }
                }
                "msgid" -> {
                    require(id == null) { "Unexpected msgid" }
                    id = value
                    append = { id = id.orEmpty() + it }
                }
                "msgid_plural" -> {
                    require(id != null && idPlural == null && translations.isEmpty()) { "Unexpected msgid_plural" }
                    idPlural = value
                    append = { idPlural = idPlural.orEmpty() + it }
                }
                else -> {
                    require(id != null) { "msgstr without msgid" }
                    require((pluralIndex != null) == (idPlural != null)) {
                        if (idPlural != null) "A plural message needs msgstr[n]" else "A singular message needs msgstr"
                    }
                    require((pluralIndex ?: 0) == translations.size) { "Expected msgstr[${translations.size}]" }
                    val index = translations.size
                    translations += value
                    append = { translations[index] += it }
                }
            }
        }

        fun append(value: String) {
            (append ?: throw IllegalArgumentException("String without a keyword"))(value)
        }

        // Adds the entry to the [entries], and starts the next one.
        fun finishInto(entries: MutableList<Entry>): EntryBuilder {
            val id = id ?: throw IllegalArgumentException("Entry without msgid")
            require(translations.isNotEmpty()) { "Entry '$id' without msgstr" }
            val entry = Entry(context = context, id = id, idPlural = idPlural, translations = translations.toList(), flags = flags.toSet())
            require(entries.none { it.context == entry.context && it.id == entry.id }) { "Duplicated entry '${context ?: id}'" }
            entries += entry
            return EntryBuilder()
        }
    }
}
