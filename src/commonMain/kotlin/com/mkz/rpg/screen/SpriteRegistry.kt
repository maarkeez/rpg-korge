package com.mkz.rpg.screen

import com.mkz.rpg.screen.feedback.FxViews
import com.mkz.rpg.shared.adapters.presentation.UiPalette
import korlibs.image.bitmap.Bitmap
import korlibs.image.bitmap.Bitmap32
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs

/**
 * Single source of the bitmaps used by the battle views.
 * Unknown ids resolve to a palette-colored placeholder instead of failing.
 */
class SpriteRegistry {
    private var units: Map<String, Bitmap> = emptyMap()
    private var portraits: Map<String, Bitmap> = emptyMap()
    private var abilities: Map<String, Bitmap> = emptyMap()
    private var effects: Map<String, Bitmap> = emptyMap()
    private var highlights: Map<Highlight, Bitmap> = emptyMap()
    private var abilitySelection: Bitmap? = null
    private val silhouettes = mutableMapOf<String, Bitmap>()
    private var loaded = false

    enum class Highlight(
        val path: String,
    ) {
        SELECTION("battlefield/tile_selection_4.png"),
        MOVEMENT("battlefield/tile_selection_3.png"),
        CAST("battlefield/tile_selection_2.png"),
        ALTERNATIVE("battlefield/tile_selection_1.png"),
    }

    /** Loads every bitmap once. Later calls do nothing. */
    suspend fun load() {
        if (loaded) return
        units = UNIT_IDS.associateWith { resourcesVfs["unit/$it.png"].readBitmap() }
        portraits = UNIT_IDS.associateWith { resourcesVfs["unit/${it}_portrait.png"].readBitmap() }
        abilities = ABILITY_IDS.associateWith { resourcesVfs["ability/${it.replace('-', '_')}.png"].readBitmap() }
        effects = EFFECT_IDS.associateWith { resourcesVfs["effect/$it.png"].readBitmap() }
        highlights = Highlight.entries.associateWith { resourcesVfs[it.path].readBitmap() }
        abilitySelection = resourcesVfs["ability/ability_selection.png"].readBitmap()
        loaded = true
    }

    fun unit(unitId: String): Bitmap = units[unitId] ?: placeholder(ART_SIZE)

    /** White copy of the unit sprite, used for hit flashes. */
    fun silhouette(unitId: String): Bitmap = silhouettes.getOrPut(unitId) { FxViews.silhouette(unit(unitId)) }

    fun portrait(unitId: String): Bitmap = portraits[unitId] ?: placeholder(PORTRAIT_SIZE)

    fun ability(abilityId: String): Bitmap = abilities[abilityId] ?: placeholder(ART_SIZE)

    fun effect(effectId: String): Bitmap = effects[effectId] ?: placeholder(ART_SIZE)

    fun highlight(highlight: Highlight): Bitmap = highlights[highlight] ?: placeholder(ART_SIZE)

    fun abilitySelection(): Bitmap = abilitySelection ?: placeholder(ART_SIZE)

    private fun placeholder(size: Int): Bitmap =
        Bitmap32(size, size).also { bitmap ->
            for (y in 0 until size) {
                for (x in 0 until size) {
                    bitmap[x, y] = UiPalette.placeholder
                }
            }
        }

    private companion object {
        const val ART_SIZE = 16
        const val PORTRAIT_SIZE = 32
        val UNIT_IDS = listOf("knight", "rat", "bee")
        val ABILITY_IDS = listOf("heal", "sword", "poisoned-sword", "mushroom", "skull", "teleport", "bee")
        val EFFECT_IDS = listOf("venom-damage", "venom-on-death")
    }
}
