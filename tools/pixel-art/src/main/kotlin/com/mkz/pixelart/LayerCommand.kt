package com.mkz.pixelart

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class LayerCommand : CliktCommand("layer") {
    override fun help(context: Context) = "Adds, removes, renames, moves, hides and shows layers."

    override fun run() = Unit

    init {
        subcommands(LayerAdd(), LayerRemove(), LayerRename(), LayerMove(), LayerVisibility(visible = false), LayerVisibility(visible = true))
    }
}

/** Where a layer goes: above or below another one, or at the top or the bottom. */
private fun Sprite.insertionIndex(
    above: String?,
    below: String?,
    top: Boolean,
    bottom: Boolean,
): Int {
    if (listOf(above != null, below != null, top, bottom).count { it } > 1) throw PixelArtException("Use only one of --above, --below, --top and --bottom.")
    return when {
        above != null -> layers.indexOf(layer(above)) + 1
        below != null -> layers.indexOf(layer(below))
        bottom -> 0
        else -> layers.size
    }
}

private class LayerAdd : EditCommand("add") {
    override fun help(context: Context) = "Adds an empty layer, at the top unless told otherwise."

    private val name by argument("NAME", help = "The new layer.")
    private val above by option("--above", help = "Right above this layer.")
    private val below by option("--below", help = "Right below this layer.")
    private val top by option("--top", help = "At the top (the default).").flag()
    private val bottom by option("--bottom", help = "At the bottom.").flag()

    override fun change(sprite: Sprite): Change {
        val layer = sprite.newLayer(name)
        sprite.layers.add(sprite.insertionIndex(above, below, top, bottom), layer)
        return Change("Added layer '$name'. Layers: ${sprite.layerNamesFromTop().joinToString()}")
    }
}

private class LayerRemove : EditCommand("remove") {
    override fun help(context: Context) = "Removes a layer and its pixels."

    private val name by argument("NAME", help = "The layer.")

    override fun change(sprite: Sprite): Change {
        sprite.layers.remove(sprite.layer(name))
        return Change("Removed layer '$name'. Layers: ${sprite.layerNamesFromTop().joinToString()}")
    }
}

private class LayerRename : EditCommand("rename") {
    override fun help(context: Context) = "Renames a layer."

    private val name by argument("NAME", help = "The layer.")
    private val newName by argument("NEW_NAME", help = "Its new name.")

    override fun change(sprite: Sprite): Change {
        val layer = sprite.layer(name)
        sprite.validateLayerName(newName)
        layer.name = newName
        return Change("Renamed layer '$name' to '$newName'.")
    }
}

private class LayerMove : EditCommand("move") {
    override fun help(context: Context) = "Moves a layer above or below another one, or to the top or the bottom."

    private val name by argument("NAME", help = "The layer.")
    private val above by option("--above", help = "Right above this layer.")
    private val below by option("--below", help = "Right below this layer.")
    private val top by option("--top", help = "At the top.").flag()
    private val bottom by option("--bottom", help = "At the bottom.").flag()

    override fun change(sprite: Sprite): Change {
        if (listOf(above != null, below != null, top, bottom).none { it }) throw PixelArtException("Tell where to move it with --above, --below, --top or --bottom.")
        val layer = sprite.layer(name)
        if (name == above || name == below) throw PixelArtException("A layer can not be moved next to itself.")
        sprite.layers.remove(layer)
        sprite.layers.add(sprite.insertionIndex(above, below, top, bottom), layer)
        return Change("Moved layer '$name'. Layers: ${sprite.layerNamesFromTop().joinToString()}")
    }
}

private class LayerVisibility(
    private val visible: Boolean,
) : EditCommand(if (visible) "show" else "hide") {
    override fun help(context: Context) = if (visible) "Shows a layer, so the composite and the PNG display it." else "Hides a layer, so the composite and the PNG do not display it."

    private val name by argument("NAME", help = "The layer.")

    override fun change(sprite: Sprite): Change {
        sprite.layer(name).visible = visible
        return Change("${if (visible) "Showed" else "Hid"} layer '$name'.")
    }
}
