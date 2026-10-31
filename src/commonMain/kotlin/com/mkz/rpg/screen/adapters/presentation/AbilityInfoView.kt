package com.mkz.rpg.screen.adapters.presentation

import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.SolidRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.Size

/**
 * What a selected ability does: its icon and name, why it can not be cast now (if so), its mana cost and cooldown, who
 * it is cast on, as a sentence and a 3x3 diagram around the caster, and one sentence per effect, wrapped to the width.
 * In the diagram, the neighbours are filled when the ability reaches all of them, and outlined when it reaches one.
 */
open class AbilityInfoView(
    private val panelWidth: Double,
    private val resourcesLoader: BattleSceneResourcesLoader,
    private val texts: Texts,
) : Container() {
    companion object {
        const val WIDTH = 380.0

        internal val BACKGROUND_COLOR = RGBA(0x14, 0x14, 0x22)
        internal val BORDER_COLOR = RGBA(0x9C, 0x9C, 0xB4)
        internal val MUTED_COLOR = RGBA(0xB4, 0xB4, 0xC8)

        // The mana bar's blue, and the cast points' orange.
        internal val MANA_COLOR = RGBA(0, 145, 255)
        internal val COOLDOWN_COLOR = BattleUnitInfoView.CAST_POINTS_COLOR

        // The diagram: the caster, an enemy, an ally, and a tile with no one.
        internal val CASTER_COLOR = Colors.WHITE
        internal val ENEMY_COLOR = RGBA(255, 55, 95)
        internal val ALLY_COLOR = RGBA(0x5A, 0xD2, 0x5A)
        internal val EMPTY_TILE_COLOR = Colors.WHITE
        private val UNMARKED_COLOR = RGBA(0x3A, 0x3A, 0x4E)

        private const val PADDING = 6.0
        private const val TEXT_SIZE = 12.0
        private const val NAME_TEXT_SIZE = 15.0
        private const val LINE_SPACING = 3.0
        private const val ICON_SCALE = 1.5
        private const val ABILITY_ICON_SIZE = 16 * ICON_SCALE
        private const val STAT_ICON_SIZE = 8 * ICON_SCALE
        private const val STAT_SPACING = 12.0
        private const val BULLET_SIZE = 3.0
        private const val BULLET_INDENT = 8.0
        private const val CELL_SIZE = 9.0
        private const val CELL_GAP = 1.0
        private const val DIAGRAM_SIZE = 3 * CELL_SIZE + 2 * CELL_GAP
    }

    /** The plain data of an ability the view displays: numbers and ids, translated by the view. */
    data class AbilityInfo(
        val abilityId: String,
        val manaCost: Int,
        val cooldown: Int,
        val targeting: Targeting,
        val effects: List<Effect>,
        // Why it can not be cast now, or null when it can, or when it is not the player's to cast.
        val reason: Reason? = null,
    ) {
        enum class Targeting {
            SELF,
            ADJACENT_ENEMY,
            ALL_ADJACENT_ENEMIES,
            VACANT_TILE_ADJACENT_TO_SELF,
            VACANT_TILE_ADJACENT_TO_BATTLE_UNIT,
        }

        sealed interface Effect {
            data class Damage(
                val damage: Int,
            ) : Effect

            data class DamageEachTurn(
                val damage: Int,
                val turns: Int,
            ) : Effect

            data class HealSelf(
                val healing: Int,
            ) : Effect

            data class HealNearbyAllies(
                val healing: Int,
            ) : Effect

            data class Summon(
                val unitId: String,
                val turns: Int,
                val healthPoints: Int,
            ) : Effect

            data object Teleport : Effect

            data class OnDefeatedPoisonAllies(
                val damage: Int,
                val turns: Int,
            ) : Effect
        }

        sealed interface Reason {
            data class Cooldown(
                val turnsLeft: Int,
            ) : Reason

            data class NotEnoughMana(
                val cost: Int,
                val remainingMana: Int,
            ) : Reason
        }
    }

    /** Who the ability reaches on a tile of the diagram. */
    enum class Mark {
        NONE,
        CASTER,
        ENEMY,
        ALLY,
        EMPTY_TILE,
    }

    private val border: SolidRect = solidRect(Size(panelWidth, 0.0), BORDER_COLOR)
    private val background: SolidRect = solidRect(Size(panelWidth - 2, 0.0), BACKGROUND_COLOR) { xy(1.0, 1.0) }
    private val content: Container = container()

    internal lateinit var nameText: Text
    internal var reasonText: Text? = null
    internal lateinit var manaCostText: Text
    internal lateinit var cooldownText: Text
    internal lateinit var targetingLines: List<Text>
    internal lateinit var effectLines: List<List<Text>>

    /** The diagram, row by row, the caster in the middle. */
    internal var marks: List<Mark> = emptyList()

    // When the ability reaches one of the neighbours marked, not all of them, they are outlined.
    internal var isOneOfTheNeighbours = false

    // Where the next line goes.
    private var cursorY = PADDING

    init {
        visible = false
    }

    open fun display(abilityInfo: AbilityInfo) {
        content.removeChildren()
        cursorY = PADDING

        // Icon, name, mana cost and cooldown, the diagram to their right
        resourcesLoader.bitmapOrNull("ability/${abilityInfo.abilityId}.png")?.let { bitmap ->
            content.image(bitmap) {
                smoothing = false
                scale = ICON_SCALE
                xy(PADDING, cursorY)
            }
        }
        nameText =
            content.text(texts.abilityName(abilityInfo.abilityId), textSize = NAME_TEXT_SIZE, color = Colors.WHITE) {
                xy(PADDING + ABILITY_ICON_SIZE + 4, cursorY + (ABILITY_ICON_SIZE - NAME_TEXT_SIZE) / 2)
            }
        displayStats(abilityInfo, startX = nameText.x + nameText.width + STAT_SPACING, y = cursorY + (ABILITY_ICON_SIZE - STAT_ICON_SIZE) / 2)
        displayDiagram(abilityInfo)
        cursorY += ABILITY_ICON_SIZE + LINE_SPACING

        // Why it can not be cast now
        reasonText =
            when (val reason = abilityInfo.reason) {
                is AbilityInfo.Reason.Cooldown -> lines(texts.plural(UiText.ABILITY_INFO_READY_IN, reason.turnsLeft, "turns" to reason.turnsLeft), COOLDOWN_COLOR).first()
                is AbilityInfo.Reason.NotEnoughMana -> lines(texts[UiText.ABILITY_INFO_NOT_ENOUGH_MANA, "cost" to reason.cost, "mana" to reason.remainingMana], MANA_COLOR).first()
                null -> null
            }

        // Who it is cast on
        targetingLines = lines(targeting(abilityInfo.targeting), MUTED_COLOR)

        // What it does
        effectLines = abilityInfo.effects.map { effect -> lines(sentence(effect), Colors.WHITE, indent = BULLET_INDENT, bullet = true) }

        val height = cursorY + PADDING - LINE_SPACING
        border.size = Size(panelWidth, height)
        background.size = Size(panelWidth - 2, height - 2)
        visible = true
    }

    open fun hide() {
        visible = false
    }

    private fun displayStats(
        abilityInfo: AbilityInfo,
        startX: Double,
        y: Double,
    ) {
        var x = startX
        content.image(resourcesLoader.bitmap("ability/info/mana.png")) {
            smoothing = false
            scale = ICON_SCALE
            xy(x, y)
        }
        x += STAT_ICON_SIZE + 3
        manaCostText = content.text("${abilityInfo.manaCost}", textSize = TEXT_SIZE, color = Colors.WHITE) { xy(x, y) }
        x += manaCostText.width + STAT_SPACING
        content.image(resourcesLoader.bitmap("ability/info/cooldown.png")) {
            smoothing = false
            scale = ICON_SCALE
            xy(x, y)
        }
        x += STAT_ICON_SIZE + 3
        val cooldown = if (abilityInfo.cooldown == 0) "-" else "${abilityInfo.cooldown}"
        cooldownText = content.text(cooldown, textSize = TEXT_SIZE, color = Colors.WHITE) { xy(x, y) }
    }

    private fun targeting(targeting: AbilityInfo.Targeting): String =
        texts[
            when (targeting) {
                AbilityInfo.Targeting.SELF -> UiText.ABILITY_INFO_TARGETING_SELF
                AbilityInfo.Targeting.ADJACENT_ENEMY -> UiText.ABILITY_INFO_TARGETING_ADJACENT_ENEMY
                AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES -> UiText.ABILITY_INFO_TARGETING_ALL_ADJACENT_ENEMIES
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_SELF -> UiText.ABILITY_INFO_TARGETING_TILE_NEXT_TO_SELF
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> UiText.ABILITY_INFO_TARGETING_TILE_NEXT_TO_UNIT
            },
        ]

    private fun sentence(effect: AbilityInfo.Effect): String =
        when (effect) {
            is AbilityInfo.Effect.Damage -> texts[UiText.ABILITY_INFO_DAMAGE, "damage" to effect.damage]
            is AbilityInfo.Effect.DamageEachTurn -> texts.plural(UiText.ABILITY_INFO_DAMAGE_EACH_TURN, effect.turns, "damage" to effect.damage, "turns" to effect.turns)
            is AbilityInfo.Effect.HealSelf -> texts[UiText.ABILITY_INFO_HEAL_SELF, "healing" to effect.healing]
            is AbilityInfo.Effect.HealNearbyAllies -> texts[UiText.ABILITY_INFO_HEAL_NEARBY_ALLIES, "healing" to effect.healing]
            is AbilityInfo.Effect.Summon ->
                texts.plural(UiText.ABILITY_INFO_SUMMON, effect.turns, "unit" to texts.unitName(effect.unitId), "turns" to effect.turns, "health" to effect.healthPoints)
            is AbilityInfo.Effect.Teleport -> texts[UiText.ABILITY_INFO_TELEPORT]
            is AbilityInfo.Effect.OnDefeatedPoisonAllies ->
                texts.plural(UiText.ABILITY_INFO_ON_DEFEATED_POISON_ALLIES, effect.turns, "damage" to effect.damage, "turns" to effect.turns)
        }

    /** The text wrapped to the width, below the last line, clear of the diagram. Returns a text per line. */
    private fun lines(
        text: String,
        color: RGBA,
        indent: Double = 0.0,
        bullet: Boolean = false,
    ): List<Text> {
        val left = PADDING + indent
        if (bullet) {
            content.solidRect(Size(BULLET_SIZE, BULLET_SIZE), color) { xy(PADDING + 1, cursorY + (TEXT_SIZE - BULLET_SIZE) / 2) }
        }
        val lines = mutableListOf<Text>()
        var line: Text? = null
        text.split(" ").forEach { word ->
            val current = line
            if (current != null) {
                val previous = current.text
                current.text = "$previous $word"
                if (current.width <= maximumWidth(left)) return@forEach
                current.text = previous
                cursorY += TEXT_SIZE + LINE_SPACING
            }
            line = content.text(word, textSize = TEXT_SIZE, color = color) { xy(left, cursorY) }.also(lines::add)
        }
        cursorY += TEXT_SIZE + LINE_SPACING
        return lines
    }

    // The lines beside the diagram end before it.
    private fun maximumWidth(left: Double): Double {
        val isBesideDiagram = cursorY < PADDING + DIAGRAM_SIZE
        val right = if (isBesideDiagram) panelWidth - PADDING - DIAGRAM_SIZE - PADDING else panelWidth - PADDING
        return right - left
    }

    private fun displayDiagram(abilityInfo: AbilityInfo) {
        marks = marks(abilityInfo)
        isOneOfTheNeighbours = abilityInfo.targeting != AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES && abilityInfo.targeting != AbilityInfo.Targeting.SELF
        val diagramX = panelWidth - PADDING - DIAGRAM_SIZE
        marks.forEachIndexed { index, mark ->
            val cellX = diagramX + (index % 3) * (CELL_SIZE + CELL_GAP)
            val cellY = PADDING + (index / 3) * (CELL_SIZE + CELL_GAP)
            val (color, isFilled) =
                when (mark) {
                    Mark.NONE -> UNMARKED_COLOR to true
                    Mark.CASTER -> CASTER_COLOR to true
                    Mark.ENEMY -> ENEMY_COLOR to !isOneOfTheNeighbours
                    Mark.ALLY -> ALLY_COLOR to true
                    Mark.EMPTY_TILE -> EMPTY_TILE_COLOR to false
                }
            content.solidRect(Size(CELL_SIZE, CELL_SIZE), color) { xy(cellX, cellY) }
            if (!isFilled) content.solidRect(Size(CELL_SIZE - 2, CELL_SIZE - 2), BACKGROUND_COLOR) { xy(cellX + 1, cellY + 1) }
        }
    }

    // The caster in the middle, and its four orthogonal neighbours marked by who the ability reaches there.
    private fun marks(abilityInfo: AbilityInfo): List<Mark> {
        val healsAllies = abilityInfo.effects.any { effect -> effect is AbilityInfo.Effect.HealNearbyAllies }
        val (caster, neighbour) =
            when (abilityInfo.targeting) {
                AbilityInfo.Targeting.SELF -> Mark.ALLY to if (healsAllies) Mark.ALLY else Mark.NONE
                AbilityInfo.Targeting.ADJACENT_ENEMY, AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES -> Mark.CASTER to Mark.ENEMY
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_SELF, AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_BATTLE_UNIT -> Mark.CASTER to Mark.EMPTY_TILE
            }
        return List(9) { index ->
            when (index) {
                4 -> caster
                1, 3, 5, 7 -> neighbour
                else -> Mark.NONE
            }
        }
    }
}
