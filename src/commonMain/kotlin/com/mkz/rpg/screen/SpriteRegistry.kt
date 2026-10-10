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
    private var fxStrips: Map<String, List<Bitmap>> = emptyMap()
    private var unitAnimations: Map<Pair<String, String>, List<Bitmap>> = emptyMap()
    private val overhangs = mutableMapOf<String, Int>()
    private val silhouettes = mutableMapOf<String, Bitmap>()
    private var loaded = false

    enum class Highlight(
        val path: String,
    ) {
        SELECTION("battlefield/tile_selection_4.png"),
    }

    /** Loads every bitmap once. Later calls do nothing. */
    suspend fun load() {
        if (loaded) return
        units = UNIT_IDS.associateWith { resourcesVfs[unitPath(it)].readBitmap() }
        portraits = UNIT_IDS.associateWith { resourcesVfs[portraitPath(it)].readBitmap() }
        abilities = ABILITY_IDS.associateWith { resourcesVfs[abilityPath(it)].readBitmap() }
        effects = EFFECT_IDS.associateWith { resourcesVfs[effectPath(it)].readBitmap() }
        highlights = Highlight.entries.associateWith { resourcesVfs[it.path].readBitmap() }
        fxStrips = FX_IDS.mapNotNull { fxId -> loadStrip(fxPath(fxId), ART_SIZE)?.let { fxId to it } }.toMap()
        unitAnimations =
            UNIT_IDS
                .flatMap { unitId ->
                    UNIT_ANIMATIONS.mapNotNull { animation ->
                        loadStrip(unitAnimationPath(unitId, animation), UNIT_FRAME_SIZE)?.let { (unitId to animation) to it }
                    }
                }.toMap()
        loaded = true
    }

    /** Frames of an authored FX strip, or null when the strip file does not exist yet (callers draw the procedural fallback). */
    fun fxFrames(fxId: String): List<Bitmap>? = fxStrips[fxId]

    /** Frames of a unit animation, or just the unit sprite when the strip does not exist. */
    fun unitFrames(
        unitId: String,
        animation: String,
    ): List<Bitmap> = unitAnimations[unitId to animation] ?: listOf(unit(unitId))

    /**
     * How many art pixels the unit rises above its tile, over every frame it can show. Unit frames are
     * [UNIT_FRAME_SIZE] square and stand on the tile: the tile covers frame rows 16..31 and columns 8..23.
     */
    fun unitOverhang(unitId: String): Int =
        overhangs.getOrPut(unitId) {
            val frames = listOf(unit(unitId)) + UNIT_ANIMATIONS.flatMap { unitFrames(unitId, it) }
            frames.maxOf { frame -> (UNIT_TILE_TOP - topOpaqueRow(frame)).coerceAtLeast(0) }
        }

    /** True when the unit's art looks to the right; mirroring it makes it look to the left, and the other way round. */
    fun facesRight(unitId: String): Boolean = unitId in UNITS_DRAWN_FACING_RIGHT

    /** The largest [unitOverhang] of every registered unit, so the map can leave room above its first row. */
    fun maxUnitOverhang(): Int = UNIT_IDS.maxOfOrNull(::unitOverhang) ?: 0

    private fun topOpaqueRow(frame: Bitmap): Int {
        for (y in 0 until frame.height) {
            for (x in 0 until frame.width) {
                if (frame.getRgba(x, y).a > 0) return y
            }
        }
        return frame.height
    }

    private suspend fun loadStrip(
        path: String,
        frameSize: Int,
    ): List<Bitmap>? {
        val file = resourcesVfs[path]
        if (!file.exists()) return null
        val strip = file.readBitmap().toBMP32()
        return List(strip.width / frameSize) { index ->
            Bitmap32(frameSize, frameSize).also { frame ->
                for (y in 0 until frameSize) {
                    for (x in 0 until frameSize) frame[x, y] = strip[index * frameSize + x, y]
                }
            }
        }
    }

    fun unit(unitId: String): Bitmap = units[unitId] ?: placeholder(UNIT_FRAME_SIZE)

    /** White copy of the unit sprite, used for hit flashes. Built from frame 0, so it is the same for every idle frame. */
    fun silhouette(unitId: String): Bitmap = silhouettes.getOrPut(unitId) { FxViews.silhouette(unit(unitId)) }

    fun portrait(unitId: String): Bitmap = portraits[unitId] ?: placeholder(PORTRAIT_SIZE)

    fun ability(abilityId: String): Bitmap = abilities[abilityId] ?: placeholder(ART_SIZE)

    fun effect(effectId: String): Bitmap = effects[effectId] ?: placeholder(ART_SIZE)

    fun highlight(highlight: Highlight): Bitmap = highlights[highlight] ?: placeholder(ART_SIZE)

    private fun placeholder(size: Int): Bitmap =
        Bitmap32(size, size).also { bitmap ->
            for (y in 0 until size) {
                for (x in 0 until size) {
                    bitmap[x, y] = UiPalette.placeholder
                }
            }
        }

    // Internal so the asset tests can check that every registered id resolves to a file.
    internal companion object {
        private const val ART_SIZE = 16

        /** Unit frames are square and stand on their tile, so units can be up to one tile taller and half a tile wider on each side. */
        const val UNIT_FRAME_SIZE = 32

        /** First frame row covered by the unit's own tile. */
        const val UNIT_TILE_TOP = 16

        /** First frame column covered by the unit's own tile. */
        const val UNIT_TILE_LEFT = 8
        private const val PORTRAIT_SIZE = 32
        internal val UNIT_IDS = listOf("knight", "rat", "bee")
        private val UNITS_DRAWN_FACING_RIGHT = setOf("knight")
        internal val ABILITY_IDS = listOf("heal", "sword", "poisoned-sword", "mushroom", "skull", "teleport", "bee")
        internal val EFFECT_IDS = listOf("venom-damage", "venom-on-death")

        internal const val FX_DEFEAT = "fx_defeat"
        internal const val FX_HIT = "fx_hit"
        internal const val FX_HEAL = "fx_heal"
        internal const val FX_VENOM = "fx_venom"
        internal const val FX_SPREAD = "fx_spread"
        internal val FX_IDS = listOf(FX_DEFEAT, FX_HIT, FX_HEAL, FX_VENOM, FX_SPREAD)

        internal fun fxPath(fxId: String): String = "effect/$fxId.png"

        const val IDLE = "idle"
        const val WALK = "walk"
        internal val UNIT_ANIMATIONS = listOf(IDLE, WALK)

        internal fun unitAnimationPath(
            unitId: String,
            animation: String,
        ): String = "unit/${unitId}_$animation.png"

        internal fun unitPath(unitId: String): String = "unit/$unitId.png"

        internal fun portraitPath(unitId: String): String = "unit/${unitId}_portrait.png"

        internal fun abilityPath(abilityId: String): String = "ability/${abilityId.replace('-', '_')}.png"

        internal fun effectPath(effectId: String): String = "effect/$effectId.png"
    }
}
