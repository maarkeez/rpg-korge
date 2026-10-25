package com.mkz.pixelart

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test

class PaletteTest {
    @Test
    fun `should hold the colours of the game's palette when is compared with it`() {
        // Given
        val image = ImageIO.read(File(System.getProperty("pixelart.resources"), "famicube-palette.png"))
        // When
        val colours = (0 until image.width).map { image.getRGB(it, 0) and 0xffffff }
        // Then
        Palette.colours shouldContainExactly colours
    }

    @Test
    fun `should give every colour its own character when is listed`() {
        // When
        val chars = (0 until Palette.size).map { Palette.char(it) }
        // Then
        chars.toSet() shouldHaveSize 65
        chars.map { Palette.indexOfChar(it) } shouldContainExactly (0 until Palette.size).toList()
        Palette.indexOfChar(Palette.KEEP_CHAR) shouldBe null
    }

    @Test
    fun `should read a colour when is given as its character, its hex or transparent`() {
        // When
        val byChar = Palette.parse("W")
        val byHex = Palette.parse("#151515")
        val byHexWithoutHash = Palette.parse("151515")
        val transparent = Palette.parse(".")
        // Then
        Palette.describe(byChar) shouldBe "#151515"
        byHex shouldBe byChar
        byHexWithoutHash shouldBe byChar
        transparent shouldBe Palette.TRANSPARENT
    }

    @Test
    fun `should fail naming the closest colour when the hex is off-palette`() {
        // When
        val exception = shouldThrow<PixelArtException> { Palette.parse("#161616") }
        // Then
        exception.message shouldBe "#161616 is not a FamiCube colour, its closest one is #151515 'W'."
    }
}
