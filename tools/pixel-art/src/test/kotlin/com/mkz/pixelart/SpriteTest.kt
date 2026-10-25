package com.mkz.pixelart

import io.kotest.matchers.shouldBe
import java.io.File
import kotlin.test.Test

class SpriteTest {
    @Test
    fun `should display the topmost opaque pixel of the displayed layers when is composited`() {
        // Given
        val sprite =
            spriteOf(
                layerOf("base", "000", "000"),
                layerOf("hidden", "===", "===", visible = false),
                layerOf("transparent", "===", "===", opacity = 0),
                layerOf("outline", "W.W", "..."),
            )
        // When
        val composite = sprite.composite()
        // Then
        composite shouldBe pixelsOf("W0W", "000")
    }

    @Test
    fun `should export to the same path under resources when is under art`() {
        // Given
        val source = File("/project/art/unit/knight.aseprite")
        // When
        val png = SpriteFiles.exportedPng(source)
        // Then
        png shouldBe File("/project/resources/unit/knight.png")
    }

    @Test
    fun `should not be exported when is not under art`() {
        SpriteFiles.exportedPng(File("/project/sketches/knight.aseprite")) shouldBe null
    }
}
