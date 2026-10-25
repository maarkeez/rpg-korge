package com.mkz.pixelart

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.image.BufferedImage

/**
 * Draws pixels upscaled for an agent to look at: every pixel is a [scale] x [scale] square, transparent pixels show a
 * checkerboard, and the coordinates are written around the image, so what is seen can be told by its x,y.
 */
object Preview {
    private const val TARGET_SIZE = 512
    private val lightChecker = Color(0xe6e6e6)
    private val darkChecker = Color(0xbdbdbd)
    private val pixelLine = Color(0x80, 0x80, 0x80, 0x70)
    private val tileLine = Color(0xff, 0x00, 0xff, 0xb0)
    private val margin = Color(0xffffff)
    private val label = Color(0x202020)

    /** The scale that makes the longest side of the sprite about [TARGET_SIZE] pixels. */
    fun defaultScale(pixels: Pixels): Int = (TARGET_SIZE / maxOf(pixels.width, pixels.height)).coerceIn(1, 64)

    fun render(
        pixels: Pixels,
        scale: Int,
        grid: Boolean,
        tile: Int,
    ): BufferedImage {
        val labelEvery =
            when {
                scale >= 16 -> 1
                scale >= 8 -> 2
                else -> 4
            }
        val fontSize = (scale * labelEvery * 0.6).toInt().coerceIn(8, 14)
        val offset = fontSize * 3
        val width = offset + pixels.width * scale
        val height = offset + pixels.height * scale
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = image.createGraphics()
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        graphics.color = margin
        graphics.fillRect(0, 0, width, height)

        val checker = maxOf(1, scale / 2)
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                val left = offset + x * scale
                val top = offset + y * scale
                val rgb = Palette.rgb(pixels[x, y])
                if (rgb == null) {
                    for (cy in 0 until scale step checker) {
                        for (cx in 0 until scale step checker) {
                            graphics.color = if ((cx / checker + cy / checker) % 2 == 0) lightChecker else darkChecker
                            graphics.fillRect(left + cx, top + cy, minOf(checker, scale - cx), minOf(checker, scale - cy))
                        }
                    }
                } else {
                    graphics.color = Color(rgb)
                    graphics.fillRect(left, top, scale, scale)
                }
            }
        }

        if (grid && scale >= 4) {
            graphics.color = pixelLine
            for (x in 0..pixels.width) graphics.drawLine(offset + x * scale, offset, offset + x * scale, height - 1)
            for (y in 0..pixels.height) graphics.drawLine(offset, offset + y * scale, width - 1, offset + y * scale)
        }
        if (grid && tile > 0) {
            graphics.color = tileLine
            graphics.stroke = BasicStroke(2f)
            for (x in 0..pixels.width step tile) graphics.drawLine(offset + x * scale, offset, offset + x * scale, height - 1)
            for (y in 0..pixels.height step tile) graphics.drawLine(offset, offset + y * scale, width - 1, offset + y * scale)
        }

        graphics.color = label
        graphics.font = Font(Font.SANS_SERIF, Font.PLAIN, fontSize)
        val metrics = graphics.fontMetrics
        for (x in 0 until pixels.width step labelEvery) {
            val text = x.toString()
            graphics.drawString(text, offset + x * scale + (scale - metrics.stringWidth(text)) / 2, offset - fontSize / 2)
        }
        for (y in 0 until pixels.height step labelEvery) {
            val text = y.toString()
            graphics.drawString(text, offset - metrics.stringWidth(text) - fontSize / 2, offset + y * scale + (scale + metrics.ascent - metrics.descent) / 2)
        }
        graphics.dispose()
        return image
    }
}
