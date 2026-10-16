package com.mkz.rpg.battlefield.adapters.resources

import com.mkz.rpg.battlefield.usecases.services.BattlefieldEncoder

/**
 * Writes the terrain id of each tile as the `battlefield/battlefield.toml` resource read by [ResourcesBattlefieldLoader]:
 * one row per line, with the terrain ids aligned in columns.
 */
class TomlBattlefieldEncoder : BattlefieldEncoder {
    private companion object {
        const val HEADER = "# Terrain id of each tile. Each line is a row of the battlefield, from top to bottom."
        const val INDENTATION = "    "

        // Spaces between a terrain id and the next one, after the longest terrain id.
        const val COLUMN_GAP = 2
    }

    override fun encode(tiles: List<List<String>>): String {
        val longestTerrainId = tiles.flatten().maxOfOrNull { terrainId -> terrainId.length } ?: 0
        // A quoted terrain id followed by its comma.
        val columnWidth = longestTerrainId + "\"\",".length + COLUMN_GAP
        val rows =
            tiles.joinToString(separator = "") { row ->
                val terrainIds = row.dropLast(1).joinToString(separator = "") { terrainId -> "\"$terrainId\",".padEnd(columnWidth) } + "\"${row.last()}\""
                "$INDENTATION[$terrainIds],\n"
            }
        return "$HEADER\ntiles = [\n$rows]\n"
    }
}
