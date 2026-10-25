package com.mkz.pixelart

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.test.Test

class GridTest {
    @Test
    fun `should print coordinates and a legend when is annotated`() {
        // Given
        val pixels = pixelsOf("W.", ".=")
        // When
        val grid = Grid.annotated(pixels, title = "title")
        // Then
        grid shouldBe
            """
            title
              01
            0 W.
            1 .=

            legend:
              . transparent (2 px)
              W #151515 (1 px)
              = #ffffff (1 px)
            """.trimIndent()
    }

    @Test
    fun `should print the tens above the units when is wider than ten pixels`() {
        // Given
        val pixels = Pixels(12, 11)
        // When
        val grid = Grid.annotated(pixels, Region(0, 9, 12, 2), title = "title").lines()
        // Then
        grid[1] shouldBe "             11"
        grid[2] shouldBe "   012345678901"
        grid[3] shouldBe " 9 ............"
        grid[4] shouldBe "10 ............"
    }

    @Test
    fun `should print only the region when is given one`() {
        // Given
        val pixels = pixelsOf("W..", ".=.", "..0")
        // When
        val grid = Grid.plain(pixels, Region(1, 1, 2, 2))
        // Then
        grid shouldBe "=.\n.0"
    }

    @Test
    fun `should read pixels to keep when is parsed`() {
        // When
        val grid = Grid.parse("  W-.\n\n  =0-  \n")
        // Then
        grid shouldBe listOf(listOf(Palette.parse("W"), null, Palette.TRANSPARENT), listOf(Palette.parse("="), Palette.parse("0"), null))
    }

    @Test
    fun `should fail naming the row and the column when a character is not a colour`() {
        // When
        val exception = shouldThrow<PixelArtException> { Grid.parse("W.\n.?") }
        // Then
        exception.message!! shouldStartWith "Grid row 1, column 1: '?' is not the character of a colour."
    }

    @Test
    fun `should read points, sizes and regions when are well formed`() {
        Point.parse("3,4") shouldBe Point(3, 4)
        Size.parse("16x32") shouldBe Size(16, 32)
        Region.parse("1,2,3x4") shouldBe Region(1, 2, 3, 4)
        shouldThrow<PixelArtException> { Size.parse("0x4") }
        shouldThrow<PixelArtException> { Region.parse("1,2") }
    }
}
