package com.mkz.pixelart

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class DrawingTest {
    private val outline = Palette.parse("W")
    private val white = Palette.parse("=")

    @Test
    fun `should paint the grid leaving the kept pixels when is painted`() {
        // Given
        val pixels = pixelsOf("0000", "0000", "0000")
        // When
        val changed = Drawing.paint(pixels, Point(1, 1), Grid.parse("W-.\n="))
        // Then
        changed shouldBe 3
        pixels shouldBe pixelsOf("0000", "0W0.", "0=00")
    }

    @Test
    fun `should fail when the grid is painted out of the sprite`() {
        // When
        val exception = shouldThrow<PixelArtException> { Drawing.paint(Pixels(2, 2), Point(1, 1), Grid.parse("WW")) }
        // Then
        exception.message shouldBe "Region 1,1,2x1 is outside of the 2x2 sprite."
    }

    @Test
    fun `should fill only the area of the colour it starts from when is filled`() {
        // Given
        val pixels = pixelsOf("..W.", "WW.W", "...W")
        // When
        val changed = Drawing.fill(pixels, Point(0, 0), white, diagonal = false)
        // Then
        changed shouldBe 2
        pixels shouldBe pixelsOf("==W.", "WW.W", "...W")
    }

    @Test
    fun `should go through the diagonals when is filled diagonally`() {
        // Given
        val pixels = pixelsOf(".W", "W.")
        // When
        Drawing.fill(pixels, Point(0, 0), white, diagonal = true)
        // Then
        pixels shouldBe pixelsOf("=W", "W=")
    }

    @Test
    fun `should draw only the border of a rectangle unless is filled`() {
        // Given
        val border = Pixels(4, 4)
        val filled = Pixels(4, 4)
        // When
        Drawing.rect(border, Region(0, 0, 4, 3), outline, filled = false)
        Drawing.rect(filled, Region(1, 1, 2, 2), outline, filled = true)
        // Then
        border shouldBe pixelsOf("WWWW", "W..W", "WWWW", "....")
        filled shouldBe pixelsOf("....", ".WW.", ".WW.", "....")
    }

    @Test
    fun `should draw the same line including both ends whichever end it starts from`() {
        // Given
        val forwards = Pixels(5, 3)
        val backwards = Pixels(5, 3)
        // When
        Drawing.line(forwards, Point(0, 0), Point(4, 2), outline)
        Drawing.line(backwards, Point(4, 2), Point(0, 0), outline)
        // Then
        forwards shouldBe pixelsOf("W....", ".WW..", "...WW")
        backwards shouldBe forwards
    }

    @Test
    fun `should replace a colour only in the region when is replaced`() {
        // Given
        val pixels = pixelsOf("WWW", "WWW")
        // When
        Drawing.replace(pixels, Region(1, 0, 2, 1), outline, white)
        // Then
        pixels shouldBe pixelsOf("W==", "WWW")
    }

    @Test
    fun `should swap the sides when is flipped`() {
        // Given
        val horizontal = pixelsOf("W.=", "...")
        val vertical = pixelsOf("W.=", "...")
        // When
        Drawing.flip(horizontal, Region.whole(horizontal), Drawing.Direction.HORIZONTAL)
        Drawing.flip(vertical, Region.whole(vertical), Drawing.Direction.VERTICAL)
        // Then
        horizontal shouldBe pixelsOf("=.W", "...")
        vertical shouldBe pixelsOf("...", "W.=")
    }

    @Test
    fun `should keep the middle column when an odd width is mirrored`() {
        // Given
        val fromLeft = pixelsOf("W0=..")
        val fromRight = pixelsOf("..=0W")
        val fromTop = pixelsOf("W", "0", ".", ".")
        // When
        Drawing.mirror(fromLeft, Region.whole(fromLeft), Drawing.Half.LEFT)
        Drawing.mirror(fromRight, Region.whole(fromRight), Drawing.Half.RIGHT)
        Drawing.mirror(fromTop, Region.whole(fromTop), Drawing.Half.TOP)
        // Then
        fromLeft shouldBe pixelsOf("W0=0W")
        fromRight shouldBe pixelsOf("W0=0W")
        fromTop shouldBe pixelsOf("W", "0", "0", "W")
    }

    @Test
    fun `should lose the pixels moved out unless is shifted wrapping`() {
        // Given
        val lost = pixelsOf("W.=")
        val wrapped = pixelsOf("W.=")
        // When
        Drawing.shift(lost, dx = -1, dy = 0, wrap = false)
        Drawing.shift(wrapped, dx = -1, dy = 0, wrap = true)
        // Then
        lost shouldBe pixelsOf(".=.")
        wrapped shouldBe pixelsOf(".=W")
    }

    @Test
    fun `should leave the pixels under the transparent ones when is copied skipping transparent`() {
        // Given
        val source = pixelsOf("W.")
        val target = pixelsOf("0000")
        // When
        Drawing.copy(source, Region(0, 0, 2, 1), target, Point(2, 0), skipTransparent = true)
        // Then
        target shouldBe pixelsOf("00W0")
    }

    @Test
    fun `should copy from the region as it was when it overlaps where it goes`() {
        // Given
        val pixels = pixelsOf("W=0.")
        // When
        Drawing.copy(pixels, Region(0, 0, 3, 1), pixels, Point(1, 0), skipTransparent = false)
        // Then
        pixels shouldBe pixelsOf("WW=0")
    }
}
