package com.mkz.pixelart

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object Png {
    const val DEFAULT_ALPHA_THRESHOLD = 128

    class Imported(
        val pixels: Pixels,
        /** The off-palette colours replaced, with their closest colour and how many pixels used them. */
        val snapped: Map<Int, Pair<Int, Int>>,
        val semiTransparent: Int,
    )

    /**
     * Reads a PNG into palette indexes. Off-palette colours and semi-transparent pixels fail, unless [snap] replaces
     * them: a colour by its closest one, an alpha below [alphaThreshold] by transparent and any other one by opaque.
     */
    fun read(
        file: File,
        snap: Boolean,
        alphaThreshold: Int = DEFAULT_ALPHA_THRESHOLD,
    ): Imported {
        val image = ImageIO.read(file) ?: throw PixelArtException("$file is not an image.")
        val pixels = Pixels(image.width, image.height)
        val offPalette = mutableMapOf<Int, Int>()
        var semiTransparent = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val argb = image.getRGB(x, y)
                val alpha = argb ushr 24
                if (alpha in 1..254) semiTransparent++
                if (alpha == 0 || (alpha < 255 && alpha < alphaThreshold)) continue
                val rgb = argb and 0xffffff
                val index = Palette.indexOfRgb(rgb)
                if (index == null) offPalette.merge(rgb, 1, Int::plus)
                pixels[x, y] = index ?: Palette.nearest(rgb)
            }
        }
        if (!snap && (offPalette.isNotEmpty() || semiTransparent > 0)) {
            val problems =
                offPalette.entries.sortedByDescending { it.value }.map { (rgb, count) ->
                    "  ${Palette.hex(rgb)} ($count px), closest is ${Palette.describe(Palette.nearest(rgb))} '${Palette.char(Palette.nearest(rgb))}'"
                } + listOfNotNull(semiTransparent.takeIf { it > 0 }?.let { "  $it semi-transparent px" })
            throw PixelArtException(
                "$file does not only use opaque FamiCube colours:\n${problems.joinToString(
                    "\n",
                )}\nRun again with --snap to replace them by their closest colour, and alphas below --alpha-threshold by transparent.",
            )
        }
        return Imported(pixels, offPalette.mapValues { (rgb, count) -> Palette.nearest(rgb) to count }, semiTransparent)
    }

    /** Writes the pixels, unless the file already holds them, so re-exporting an unchanged sprite changes no file. */
    fun write(
        pixels: Pixels,
        file: File,
    ) {
        if (file.isFile && holds(file, pixels)) return
        val image = BufferedImage(pixels.width, pixels.height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0 until pixels.height) {
            for (x in 0 until pixels.width) {
                image.setRGB(x, y, Palette.rgb(pixels[x, y])?.let { it or (0xff shl 24) } ?: 0)
            }
        }
        writeImage(image, file)
    }

    private fun holds(
        file: File,
        pixels: Pixels,
    ): Boolean {
        val image = runCatching { ImageIO.read(file) }.getOrNull() ?: return false
        if (image.width != pixels.width || image.height != pixels.height) return false
        return (0 until pixels.height).all { y ->
            (0 until pixels.width).all { x ->
                val argb = image.getRGB(x, y)
                val rgb = Palette.rgb(pixels[x, y])
                if (rgb == null) argb ushr 24 == 0 else argb == (rgb or (0xff shl 24))
            }
        }
    }

    fun writeImage(
        image: BufferedImage,
        file: File,
    ) {
        file.absoluteFile.parentFile.mkdirs()
        val temporary = File(file.absoluteFile.parentFile, ".${file.name}.tmp")
        if (!ImageIO.write(image, "png", temporary)) throw PixelArtException("Could not write $file.")
        SpriteFiles.replace(temporary, file)
    }
}
