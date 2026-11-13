package com.mkz.rpg.localization.domain

import com.mkz.rpg.localization.domain.LocalizationError.EmptyLanguageCode
import com.mkz.rpg.localization.domain.LocalizationError.InvalidPluralRule
import com.mkz.rpg.localization.domain.LocalizationError.LanguageNotFound
import com.mkz.rpg.localization.domain.LocalizationError.TooManyPluralForms
import com.mkz.rpg.localization.domain.LocalizationEvent.LanguageSelected
import com.mkz.rpg.localization.domain.LocalizationEvent.LocalizationInitialised

@ConsistentCopyVisibility
data class Localization private constructor(
    private val sourceCatalog: Catalog,
    private val translationCatalogs: List<Catalog>,
    private val selectedLanguageCode: String,
    private val events: Set<LocalizationEvent>,
) {
    companion object {
        const val LANGUAGE_NAME_KEY = "language.name"

        fun create(dto: Dto): Localization {
            val sourceCatalog = Catalog.of(dto.sourceCatalog)
            val translationCatalogs = dto.translationCatalogs.map(Catalog::of).sortedBy(Catalog::languageCode)
            val languageCodes = listOf(sourceCatalog.languageCode) + translationCatalogs.map(Catalog::languageCode)
            if (dto.selectedLanguageCode !in languageCodes) throw LanguageNotFound(dto.selectedLanguageCode)
            return Localization(
                sourceCatalog = sourceCatalog,
                translationCatalogs = translationCatalogs,
                selectedLanguageCode = dto.selectedLanguageCode,
                events = setOf(LocalizationInitialised(languageCodes = languageCodes, selectedLanguageCode = dto.selectedLanguageCode)),
            )
        }

        private val PLACEHOLDER = Regex("""\{([A-Za-z][A-Za-z0-9]*)\}""")
    }

    fun pullEvents(): Pair<Set<LocalizationEvent>, Localization> = events to copy(events = emptySet())

    fun toDto() =
        Dto(
            sourceCatalog = sourceCatalog.toDto(),
            translationCatalogs = translationCatalogs.map(Catalog::toDto),
            selectedLanguageCode = selectedLanguageCode,
        )

    fun translate(
        key: String,
        arguments: Map<String, String> = emptyMap(),
    ): String = format(message(key, count = null) ?: key, arguments)

    fun translatePlural(
        key: String,
        count: Int,
        arguments: Map<String, String> = emptyMap(),
    ): String = format(message(key, count) ?: key, arguments)

    fun selectLanguage(languageCode: String): Localization {
        if (catalog(languageCode) == null) throw LanguageNotFound(languageCode)
        return copy(selectedLanguageCode = languageCode, events = events + LanguageSelected(languageCode))
    }

    fun languages(): List<Dto.LanguageDto> =
        (listOf(sourceCatalog) + translationCatalogs).map { catalog ->
            val name = catalog.message(LANGUAGE_NAME_KEY, count = null) ?: sourceCatalog.message(LANGUAGE_NAME_KEY, count = null) ?: catalog.languageCode
            Dto.LanguageDto(code = catalog.languageCode, name = name)
        }

    private fun message(
        key: String,
        count: Int?,
    ): String? = catalog(selectedLanguageCode)?.message(key, count) ?: sourceCatalog.message(key, count)

    private fun catalog(languageCode: String): Catalog? = (listOf(sourceCatalog) + translationCatalogs).firstOrNull { catalog -> catalog.languageCode == languageCode }

    private fun format(
        message: String,
        arguments: Map<String, String>,
    ): String = PLACEHOLDER.replace(message) { placeholder -> arguments[placeholder.groupValues[1]] ?: placeholder.value }

    data class Dto(
        val sourceCatalog: CatalogDto,
        val translationCatalogs: List<CatalogDto>,
        val selectedLanguageCode: String,
    ) {
        data class CatalogDto(
            val languageCode: String,
            val pluralRule: String,
            val messages: List<MessageDto>,
        )

        data class MessageDto(
            val key: String,
            val forms: List<String>,
        )

        data class LanguageDto(
            val code: String,
            val name: String,
        )
    }

    private data class Catalog(
        val languageCode: String,
        val pluralRule: PluralRule,
        val messages: Map<String, List<String>>,
    ) {
        companion object {
            fun of(dto: Dto.CatalogDto): Catalog {
                if (dto.languageCode.isEmpty()) throw EmptyLanguageCode()
                val pluralRule = PluralRule.parse(dto.languageCode, dto.pluralRule)
                dto.messages.forEach { message ->
                    if (message.forms.size > pluralRule.formCount) throw TooManyPluralForms(dto.languageCode, message.key, pluralRule.formCount)
                }
                return Catalog(
                    languageCode = dto.languageCode,
                    pluralRule = pluralRule,
                    messages = dto.messages.filter { message -> message.forms.isNotEmpty() }.associate { message -> message.key to message.forms },
                )
            }
        }

        fun message(
            key: String,
            count: Int?,
        ): String? {
            val forms = messages[key] ?: return null
            return forms.getOrNull(if (count == null) 0 else pluralRule.formIndex(count))
        }

        fun toDto() =
            Dto.CatalogDto(
                languageCode = languageCode,
                pluralRule = pluralRule.definition,
                messages = messages.map { (key, forms) -> Dto.MessageDto(key = key, forms = forms) },
            )
    }

    private class PluralRule(
        val definition: String,
        val formCount: Int,
        private val formula: (Long) -> Long,
    ) {
        companion object {
            fun parse(
                languageCode: String,
                definition: String,
            ): PluralRule {
                val fields =
                    definition
                        .split(";")
                        .map(String::trim)
                        .filter(String::isNotEmpty)
                        .associate { field -> field.substringBefore("=").trim() to field.substringAfter("=", missingDelimiterValue = "").trim() }
                val formCount = fields["nplurals"]?.toIntOrNull()?.takeIf { it > 0 } ?: throw InvalidPluralRule(languageCode, definition)
                val expression = fields["plural"]?.takeIf(String::isNotEmpty) ?: throw InvalidPluralRule(languageCode, definition)
                val formula =
                    try {
                        Parser(expression).parse()
                    } catch (_: IllegalArgumentException) {
                        throw InvalidPluralRule(languageCode, definition)
                    }
                return PluralRule(definition, formCount, formula)
            }
        }

        fun formIndex(count: Int): Int = formula(count.toLong()).toInt()

        private class Parser(
            private val expression: String,
        ) {
            private val tokens = Regex("""\s*(\d+|n|&&|\|\||==|!=|<=|>=|[!*/%+\-<>?:()])""").findAll(expression).toList()
            private var position = 0

            fun parse(): (Long) -> Long {
                require(tokens.joinToString("") { it.value }.length == expression.trimEnd().length) { "Unexpected character in '$expression'" }
                val formula = ternary()
                require(position == tokens.size) { "Unexpected '${peek()}' in '$expression'" }
                return formula
            }

            private fun ternary(): (Long) -> Long {
                val condition = logicalOr()
                if (!accept("?")) return condition
                val whenTrue = ternary()
                expect(":")
                val whenFalse = ternary()
                return { n -> if (condition(n) != 0L) whenTrue(n) else whenFalse(n) }
            }

            private fun logicalOr(): (Long) -> Long = binary(::logicalAnd, "||") { _, left, right -> (left != 0L || right != 0L).toLong() }

            private fun logicalAnd(): (Long) -> Long = binary(::equality, "&&") { _, left, right -> (left != 0L && right != 0L).toLong() }

            private fun equality(): (Long) -> Long = binary(::relational, "==", "!=") { operator, left, right -> (if (operator == "==") left == right else left != right).toLong() }

            private fun relational(): (Long) -> Long =
                binary(::additive, "<", "<=", ">", ">=") { operator, left, right ->
                    when (operator) {
                        "<" -> left < right
                        "<=" -> left <= right
                        ">" -> left > right
                        else -> left >= right
                    }.toLong()
                }

            private fun additive(): (Long) -> Long = binary(::multiplicative, "+", "-") { operator, left, right -> if (operator == "+") left + right else left - right }

            private fun multiplicative(): (Long) -> Long =
                binary(::unary, "*", "/", "%") { operator, left, right ->
                    when (operator) {
                        "*" -> left * right
                        "/" -> if (right == 0L) 0L else left / right
                        else -> if (right == 0L) 0L else left % right
                    }
                }

            private fun unary(): (Long) -> Long {
                if (!accept("!")) return primary()
                val operand = unary()
                return { n -> (operand(n) == 0L).toLong() }
            }

            private fun primary(): (Long) -> Long {
                val token = next()
                if (token == "n") return { n -> n }
                if (token == "(") return ternary().also { expect(")") }
                val value = token.toLongOrNull() ?: throw IllegalArgumentException("Unexpected '$token' in '$expression'")
                return { _ -> value }
            }

            private fun binary(
                operand: () -> (Long) -> Long,
                vararg operators: String,
                operation: (operator: String, left: Long, right: Long) -> Long,
            ): (Long) -> Long {
                var formula = operand()
                while (peek() in operators) {
                    val operator = next()
                    val left = formula
                    val right = operand()
                    formula = { n -> operation(operator, left(n), right(n)) }
                }
                return formula
            }

            private fun peek(): String? = tokens.getOrNull(position)?.groupValues?.get(1)

            private fun next(): String = peek()?.also { position++ } ?: throw IllegalArgumentException("Unexpected end of '$expression'")

            private fun accept(token: String): Boolean = (peek() == token).also { accepted -> if (accepted) position++ }

            private fun expect(token: String) {
                require(accept(token)) { "Expected '$token' in '$expression'" }
            }

            private fun Boolean.toLong(): Long = if (this) 1L else 0L
        }
    }
}
