package com.mkz.rpg.shared.adapters.toml

import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TomlServiceTest {
    @Serializable
    data class GameConfig(
        val title: String,
        val version: Int,
    )

    private val tomlService = TomlService()

    @Test
    fun `should deserialize a toml string into the expected model`() {
        // Given
        val toml =
            """
            title = "Hello World"
            version = 1
            """.trimIndent()
        // When
        val config = tomlService.deserialize<GameConfig>(toml)
        // Then
        assertThat(config).isEqualTo(GameConfig(title = "Hello World", version = 1))
    }
}
