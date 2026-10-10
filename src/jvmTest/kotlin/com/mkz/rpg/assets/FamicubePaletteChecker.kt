package com.mkz.rpg.assets

import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs

// Game art assets that must be drawn exclusively with the Famicube palette.
//
// Excluded on purpose:
// - korge.png: the Korge engine logo, not game art.
// - unit/goblins/: reference samples, not used by the game.
internal val GAME_ART_ASSET_PATHS: List<String> =
    listOf(
        "ability/bee.png",
        "ability/heal.png",
        "ability/mushroom.png",
        "ability/poisoned_sword.png",
        "ability/skull.png",
        "ability/sword.png",
        "ability/teleport.png",
        "battlefield/tile_selection_4.png",
        "effect/venom-damage.png",
        "effect/fx_defeat.png",
        "effect/fx_heal.png",
        "effect/fx_hit.png",
        "effect/fx_spread.png",
        "effect/fx_venom.png",
        "effect/venom-on-death.png",
        "terrain/dual/sand_to_grass.png",
        "terrain/dual/sand_to_lava.png",
        "terrain/dual/sand_to_void.png",
        "terrain/dual/sand_to_water.png",
        "terrain/transitions/sand_to_grass.png",
        "terrain/transitions/sand_to_lava.png",
        "terrain/transitions/sand_to_void.png",
        "terrain/transitions/sand_to_water.png",
        "terrain/variants/grass.png",
        "terrain/variants/sand.png",
        "terrain/variants/water.png",
        "unit/bee.png",
        "unit/bee_idle.png",
        "unit/bee_portrait.png",
        "unit/bee_walk.png",
        "unit/knight.png",
        "unit/knight_idle.png",
        "unit/knight_portrait.png",
        "unit/knight_walk.png",
        "unit/rat.png",
        "unit/rat_idle.png",
        "unit/rat_portrait.png",
        "unit/rat_walk.png",
    )

internal val CONTACT_SHEET_ASSET_PATHS: List<String> =
    GAME_ART_ASSET_PATHS +
        listOf(
            "unit/goblins/goblin.png",
        )

internal const val FAMICUBE_PALETTE_PATH: String = "famicube-palette.png"
internal const val CONTACT_SHEET_PATH: String = "build/contact-sheet.png"

internal data class PaletteViolation(
    val color: Int,
    val count: Int,
)

internal suspend fun loadFamicubePalette(): Set<Int> = resourcesVfs[FAMICUBE_PALETTE_PATH].readBitmap().toBMP32().paletteColors()

internal fun Bitmap32.paletteColors(): Set<Int> {
    val colors = HashSet<Int>()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this[x, y]
            if (pixel.a != 0) {
                colors += pixel.rgb
            }
        }
    }
    return colors
}

internal suspend fun checkAsset(
    path: String,
    palette: Set<Int>,
): List<PaletteViolation> {
    val bitmap = resourcesVfs[path].readBitmap().toBMP32()
    return bitmap.checkPalette(palette)
}

internal fun Bitmap32.checkPalette(palette: Set<Int>): List<PaletteViolation> {
    val violations = HashMap<Int, Int>()
    for (y in 0 until height) {
        for (x in 0 until width) {
            val pixel = this[x, y]
            if (pixel.a == 0) continue
            val color = pixel.rgb
            if (color !in palette) {
                violations[color] = (violations[color] ?: 0) + 1
            }
        }
    }
    return violations.entries
        .sortedWith(compareByDescending<Map.Entry<Int, Int>> { it.value }.thenBy { it.key })
        .map { PaletteViolation(color = it.key, count = it.value) }
}

internal fun describeViolations(
    path: String,
    violations: List<PaletteViolation>,
): String =
    violations
        .joinToString(", ") { violation -> "${colorHex(violation.color)} x ${violation.count}" }
        .let { "$path: $it" }

internal fun colorHex(color: Int): String =
    color
        .toUInt()
        .toString(16)
        .padStart(6, '0')
        .uppercase()
        .let { "#$it" }
