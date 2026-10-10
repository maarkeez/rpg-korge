package com.mkz.rpg.assets

import com.mkz.rpg.shared.adapters.presentation.UiPalette
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class UiPaletteTest {
    @Test
    fun `should contain only Famicube colors when UI palette tokens are defined`() {
        // Given
        val famicubePalette = runBlocking { loadFamicubePalette() }
        // When
        val uiColors = UiPalette.all.map { it.rgb }
        // Then
        assertThat(famicubePalette).containsAll(uiColors)
    }

    @Test
    fun `should list every token when UI palette tokens are defined`() {
        // Given
        val tokenCount = UiPalette::class.java.declaredFields.count { it.type.name == "int" && !it.name.startsWith("$") }
        // When
        val listedCount = UiPalette.all.size
        // Then
        assertThat(listedCount).isEqualTo(tokenCount)
    }
}
