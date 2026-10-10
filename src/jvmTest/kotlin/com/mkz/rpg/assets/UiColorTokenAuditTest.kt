package com.mkz.rpg.assets

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class UiColorTokenAuditTest {
    private val auditedDirectories =
        listOf(
            "src/commonMain/kotlin/com/mkz/rpg/screen",
            "src/commonMain/kotlin/com/mkz/rpg/shared/adapters/presentation",
        )
    private val colorLiteral = Regex("""Colors\.(?!TRANSPARENT\b)\w+|RGBA\(""")

    @Test
    fun `should define colors only in the UI palette when the screen and presentation sources are audited`() {
        // Given
        val sources =
            auditedDirectories
                .flatMap { File(it).walkTopDown().filter { file -> file.extension == "kt" }.toList() }
                .filter { it.name != "UiPalette.kt" }
        // When
        val offenders =
            sources.flatMap { file ->
                file
                    .readLines()
                    .withIndex()
                    .filter { (_, line) -> colorLiteral.containsMatchIn(line) }
                    .map { (index, _) -> "${file.name}:${index + 1}" }
            }
        // Then
        assertThat(sources).isNotEmpty
        assertThat(offenders).isEmpty()
    }
}
