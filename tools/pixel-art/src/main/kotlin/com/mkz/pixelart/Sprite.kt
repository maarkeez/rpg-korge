package com.mkz.pixelart

/** An image of [width] x [height] palette indexes, row by row from the top-left pixel. */
class Pixels(
    val width: Int,
    val height: Int,
    private val indexes: IntArray = IntArray(width * height),
) {
    init {
        require(indexes.size == width * height) { "Expected ${width * height} pixels, got ${indexes.size}" }
    }

    operator fun get(
        x: Int,
        y: Int,
    ): Int = indexes[y * width + x]

    operator fun set(
        x: Int,
        y: Int,
        index: Int,
    ) {
        indexes[y * width + x] = index
    }

    fun contains(
        x: Int,
        y: Int,
    ): Boolean = x in 0 until width && y in 0 until height

    fun copy(): Pixels = Pixels(width, height, indexes.copyOf())

    fun isEmpty(): Boolean = indexes.all { it == Palette.TRANSPARENT }

    /** The palette indexes used, transparent excluded, with how many pixels use each one. */
    fun colourCounts(): Map<Int, Int> =
        indexes
            .filter { it != Palette.TRANSPARENT }
            .groupingBy { it }
            .eachCount()
            .toSortedMap()

    override fun equals(other: Any?): Boolean = other is Pixels && width == other.width && height == other.height && indexes.contentEquals(other.indexes)

    override fun hashCode(): Int = indexes.contentHashCode()
}

class Layer(
    var name: String,
    val pixels: Pixels,
    var visible: Boolean = true,
    var opacity: Int = 255,
) {
    /** Whether the layer is part of what the sprite displays. */
    val displayed: Boolean get() = visible && opacity > 0
}

/**
 * A single frame sprite. Its [layers] are ordered from the bottom one to the top one, as an Aseprite file stores them,
 * while the commands list them from the top one, as Aseprite displays them.
 */
class Sprite(
    val width: Int,
    val height: Int,
    val layers: MutableList<Layer> = mutableListOf(),
) {
    fun layer(name: String): Layer = layers.find { it.name == name } ?: throw PixelArtException("There is no layer '$name'. The layers are: ${layerNamesFromTop().joinToString()}.")

    fun layerOrNull(name: String): Layer? = layers.find { it.name == name }

    fun layerNamesFromTop(): List<String> = layers.reversed().map { it.name }

    fun newLayer(name: String): Layer {
        validateLayerName(name)
        return Layer(name, Pixels(width, height))
    }

    fun validateLayerName(name: String) {
        if (name.isBlank()) throw PixelArtException("A layer name can not be blank.")
        if (layerOrNull(name) != null) throw PixelArtException("There is already a layer '$name'.")
    }

    /** What all the displayed layers show together: the topmost opaque pixel wins. */
    fun composite(): Pixels {
        val composite = Pixels(width, height)
        for (layer in layers.filter { it.displayed }) {
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val index = layer.pixels[x, y]
                    if (index != Palette.TRANSPARENT) composite[x, y] = index
                }
            }
        }
        return composite
    }

    /** The pixels of a layer, or the composite when [layerName] is `null`. */
    fun pixels(layerName: String?): Pixels = layerName?.let { layer(it).pixels } ?: composite()

    companion object {
        const val MAX_SIZE = 1024
    }
}
