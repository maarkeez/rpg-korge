package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.HudStyle.panel
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.view.Container
import korlibs.korge.view.RoundRect
import korlibs.korge.view.Text
import korlibs.korge.view.container
import korlibs.korge.view.image
import korlibs.korge.view.roundRect
import korlibs.korge.view.solidRect
import korlibs.korge.view.text
import korlibs.korge.view.xy
import korlibs.math.geom.RectCorners
import korlibs.math.geom.Size
import korlibs.math.geom.degrees

/**
 * What a selected ability does, as a card: its icon at 2x in a slot, its name and who it is cast on, and its costs as
 * pills (its mana always, its cooldown only when it has one). Why it can not be cast now, if so, is a tinted banner under
 * them. Then a sentence per effect, led by a gold diamond, its numbers in gold, wrapped beside a 3x3 diagram around the
 * caster. In the diagram, the adjacent tiles are filled when the ability reaches all of them, and outlined when it reaches one.
 */
open class AbilityInfoView(
    private val panelWidth: Double,
    private val resourcesLoader: GameSceneResourcesLoader,
    private val messages: Messages,
) : Container() {
    companion object {
        const val WIDTH = 380.0

        // The mana bar's blue, and the cast points' orange.
        internal val MANA_COLOR = HudStyle.MANA
        internal val COOLDOWN_COLOR = BattleUnitInfoView.CAST_POINTS_COLOR

        // The diagram: the caster, an enemy, an ally, and a tile with no one.
        internal val CASTER_COLOR = Colors.WHITE
        internal val ENEMY_COLOR = HudStyle.ENEMY
        internal val ALLY_COLOR = HudStyle.ALLY
        internal val EMPTY_TILE_COLOR = Colors.WHITE
        private val UNMARKED_COLOR = RGBA(0x2A, 0x2A, 0x40)

        private const val PADDING = 10.0
        private const val SPACING = 8.0
        private const val TEXT_SIZE = 12.0
        private const val NAME_TEXT_SIZE = 16.0
        private const val LINE_SPACING = 3.0
        private const val EFFECT_SPACING = 4.0
        private const val ICON_SCALE = 2.0
        private const val ICON_SLOT_SIZE = 40.0
        private const val NAME_X = PADDING + ICON_SLOT_SIZE + 8
        private const val PILL_SPACING = 6.0
        private const val BANNER_HEIGHT = 24.0
        private const val BANNER_RADIUS = 6.0
        private const val BULLET_SIZE = 5.0
        private const val BULLET_INDENT = 14.0
        private const val CELL_SIZE = 10.0
        private const val CELL_GAP = 2.0
        private const val DIAGRAM_SIZE = 3 * CELL_SIZE + 2 * CELL_GAP
        private const val DIAGRAM_MARGIN = 10.0
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
            CASTER,
            ADJACENT_ENEMY,
            ALL_ADJACENT_ENEMIES,
            VACANT_TILE_ADJACENT_TO_CASTER,
            VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT,
        }

        sealed interface Effect {
            data class Damage(
                val damage: Int,
            ) : Effect

            data class DamageOnPlayerTurnStarted(
                val damage: Int,
                val turns: Int,
            ) : Effect

            data class HealCaster(
                val healing: Int,
            ) : Effect

            data class HealAdjacentAllies(
                val healing: Int,
            ) : Effect

            data class Summon(
                val unitId: String,
                val turns: Int,
                val healthPoints: Int,
            ) : Effect

            data object Teleport : Effect

            data class OnDefeatedDamageAdjacentAllies(
                val damage: Int,
                val turns: Int,
            ) : Effect
        }

        sealed interface Reason {
            data class Cooldown(
                val turnsLeft: Int,
            ) : Reason

            data class NotEnoughMana(
                val manaCost: Int,
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

    private val background: RoundRect = panel(Size(panelWidth, ICON_SLOT_SIZE + 2 * PADDING))
    private val content: Container = container()

    internal lateinit var nameText: Text
    internal lateinit var manaCostPill: PillView
    internal var cooldownPill: PillView? = null
    internal var reasonText: Text? = null
    internal var reasonBanner: RoundRect? = null
    internal lateinit var targetingText: Text

    // The words of each effect, the numbers among them in gold.
    internal lateinit var effectWords: List<List<Text>>

    /** The diagram, row by row, the caster in the middle. */
    internal var marks: List<Mark> = emptyList()

    // When the ability reaches one of the adjacent tiles marked, not all of them, they are outlined.
    internal var isOneOfTheAdjacentTiles = false

    init {
        visible = false
    }

    open fun display(abilityInfo: AbilityInfo) {
        content.removeChildren()

        // The header: the icon in its slot, the name and who it is cast on, and the costs on the right
        content.panel(Size(ICON_SLOT_SIZE, ICON_SLOT_SIZE), fill = HudStyle.PANEL_RAISED, radius = HudStyle.SLOT_RADIUS).xy(PADDING, PADDING)
        resourcesLoader.bitmapOrNull("ability/${abilityInfo.abilityId}.png")?.let { bitmap ->
            content.image(bitmap) {
                smoothing = false
                scale = ICON_SCALE
                xy(PADDING + (ICON_SLOT_SIZE - bitmap.width * ICON_SCALE) / 2, PADDING + (ICON_SLOT_SIZE - bitmap.height * ICON_SCALE) / 2)
            }
        }
        nameText = content.text(messages.abilityName(abilityInfo.abilityId), textSize = NAME_TEXT_SIZE, color = Colors.WHITE) { xy(NAME_X, PADDING + 2) }
        targetingText = content.text(targeting(abilityInfo.targeting), textSize = TEXT_SIZE, color = HudStyle.MUTED) { xy(NAME_X, PADDING + 24) }
        displayCosts(abilityInfo)
        var cursorY = PADDING + ICON_SLOT_SIZE + SPACING

        // Why it can not be cast now
        reasonText = null
        reasonBanner = null
        reason(abilityInfo.reason)?.let { (reason, color) ->
            reasonBanner =
                content.roundRect(Size(panelWidth - 2 * PADDING, BANNER_HEIGHT), RectCorners(BANNER_RADIUS), fill = color.withAd(0.16), stroke = color.withAd(0.6), strokeThickness = 1.0) {
                    xy(PADDING, cursorY)
                }
            reasonText = content.text(reason, textSize = TEXT_SIZE, color = color) { xy(PADDING + 10, cursorY + (BANNER_HEIGHT - TEXT_SIZE) / 2 - 1) }
            cursorY += BANNER_HEIGHT + SPACING
        }

        content.solidRect(Size(panelWidth - 2 * PADDING, 1.0), HudStyle.STROKE) { xy(PADDING, cursorY) }
        cursorY += SPACING

        // What it does, beside the diagram
        val effectsTop = cursorY
        displayDiagram(abilityInfo, top = effectsTop)
        effectWords =
            abilityInfo.effects.map { effect ->
                content.solidRect(Size(BULLET_SIZE, BULLET_SIZE), HudStyle.SELECTION) {
                    rotation = 45.degrees
                    xy(PADDING + 4, cursorY + 3)
                }
                val (words, height) = wrappedWords(sentence(effect), top = cursorY)
                cursorY += height + EFFECT_SPACING
                words
            }
        cursorY = maxOf(cursorY - EFFECT_SPACING, effectsTop + DIAGRAM_SIZE)

        background.size = Size(panelWidth, cursorY + PADDING)
        visible = true
    }

    open fun hide() {
        visible = false
    }

    // The mana pill always, the cooldown pill only when the ability has one, right aligned.
    private fun displayCosts(abilityInfo: AbilityInfo) {
        var right = panelWidth - PADDING
        cooldownPill =
            if (abilityInfo.cooldown > 0) {
                PillView(color = COOLDOWN_COLOR, glyph = PillView.Glyph.COOLDOWN).also { pill ->
                    pill.display("${abilityInfo.cooldown}")
                    content.addChild(pill)
                    pill.xy(right - pill.width, PADDING + 2)
                    right -= pill.width + PILL_SPACING
                }
            } else {
                null
            }
        manaCostPill =
            PillView(color = MANA_COLOR, glyph = PillView.Glyph.MANA).also { pill ->
                pill.display("${abilityInfo.manaCost}")
                content.addChild(pill)
                pill.xy(right - pill.width, PADDING + 2)
            }
    }

    private fun reason(reason: AbilityInfo.Reason?): Pair<String, RGBA>? =
        when (reason) {
            is AbilityInfo.Reason.Cooldown -> messages.plural(UiMessage.ABILITY_INFO_READY_IN, reason.turnsLeft, "turns" to reason.turnsLeft) to COOLDOWN_COLOR
            is AbilityInfo.Reason.NotEnoughMana -> messages[UiMessage.ABILITY_INFO_NOT_ENOUGH_MANA, "manaCost" to reason.manaCost, "mana" to reason.remainingMana] to MANA_COLOR
            null -> null
        }

    private fun targeting(targeting: AbilityInfo.Targeting): String =
        messages[
            when (targeting) {
                AbilityInfo.Targeting.CASTER -> UiMessage.ABILITY_INFO_TARGETING_CASTER
                AbilityInfo.Targeting.ADJACENT_ENEMY -> UiMessage.ABILITY_INFO_TARGETING_ADJACENT_ENEMY
                AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES -> UiMessage.ABILITY_INFO_TARGETING_ALL_ADJACENT_ENEMIES
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_CASTER -> UiMessage.ABILITY_INFO_TARGETING_VACANT_TILE_ADJACENT_TO_CASTER
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> UiMessage.ABILITY_INFO_TARGETING_VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT
            },
        ]

    private fun sentence(effect: AbilityInfo.Effect): String =
        when (effect) {
            is AbilityInfo.Effect.Damage -> messages[UiMessage.ABILITY_INFO_DAMAGE, "damage" to effect.damage]
            is AbilityInfo.Effect.DamageOnPlayerTurnStarted -> messages.plural(UiMessage.ABILITY_INFO_DAMAGE_ON_PLAYER_TURN_STARTED, effect.turns, "damage" to effect.damage, "turns" to effect.turns)
            is AbilityInfo.Effect.HealCaster -> messages[UiMessage.ABILITY_INFO_HEAL_CASTER, "heal" to effect.healing]
            is AbilityInfo.Effect.HealAdjacentAllies -> messages[UiMessage.ABILITY_INFO_HEAL_ADJACENT_ALLIES, "heal" to effect.healing]
            is AbilityInfo.Effect.Summon ->
                messages.plural(UiMessage.ABILITY_INFO_SUMMON, effect.turns, "unit" to messages.unitName(effect.unitId), "turns" to effect.turns, "health" to effect.healthPoints)
            is AbilityInfo.Effect.Teleport -> messages[UiMessage.ABILITY_INFO_TELEPORT]
            is AbilityInfo.Effect.OnDefeatedDamageAdjacentAllies ->
                messages.plural(UiMessage.ABILITY_INFO_ON_DEFEATED_DAMAGE_ADJACENT_ALLIES, effect.turns, "damage" to effect.damage, "turns" to effect.turns)
        }

    /**
     * The sentence wrapped to the width left of the diagram, a text per word, the words with a number in gold. Returns
     * the words and the height of the lines.
     */
    private fun wrappedWords(
        sentence: String,
        top: Double,
    ): Pair<List<Text>, Double> {
        val left = PADDING + BULLET_INDENT
        val right = panelWidth - PADDING - DIAGRAM_SIZE - DIAGRAM_MARGIN
        val space = Text(" ", textSize = TEXT_SIZE).width
        var x = left
        var y = top
        val words =
            sentence.split(" ").map { word ->
                val isNumber = word.any(Char::isDigit)
                content.text(word, textSize = TEXT_SIZE, color = if (isNumber) HudStyle.SELECTION else Colors.WHITE).also { text ->
                    if (x > left && x + text.width > right) {
                        x = left
                        y += TEXT_SIZE + LINE_SPACING
                    }
                    text.xy(x, y)
                    x += text.width + space
                }
            }
        return words to (y + TEXT_SIZE - top)
    }

    private fun displayDiagram(
        abilityInfo: AbilityInfo,
        top: Double,
    ) {
        marks = marks(abilityInfo)
        isOneOfTheAdjacentTiles = abilityInfo.targeting != AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES && abilityInfo.targeting != AbilityInfo.Targeting.CASTER
        val diagramX = panelWidth - PADDING - DIAGRAM_SIZE
        val radius = CELL_SIZE * 0.22
        marks.forEachIndexed { index, mark ->
            val cellX = diagramX + (index % 3) * (CELL_SIZE + CELL_GAP)
            val cellY = top + (index / 3) * (CELL_SIZE + CELL_GAP)
            val (color, isFilled) =
                when (mark) {
                    Mark.NONE -> UNMARKED_COLOR to true
                    Mark.CASTER -> CASTER_COLOR to true
                    Mark.ENEMY -> ENEMY_COLOR to !isOneOfTheAdjacentTiles
                    Mark.ALLY -> ALLY_COLOR to true
                    Mark.EMPTY_TILE -> EMPTY_TILE_COLOR to false
                }
            // An outlined cell is inset by half its outline, as a stroke is drawn half outside its shape, which cuts it.
            val inset = if (isFilled) 0.0 else 0.75
            content.roundRect(
                Size(CELL_SIZE - 2 * inset, CELL_SIZE - 2 * inset),
                RectCorners(radius),
                fill = if (isFilled) color else color.withAd(0.12),
                stroke = color,
                strokeThickness = if (isFilled) 0.0 else 1.5,
            ) { xy(cellX + inset, cellY + inset) }
        }
    }

    // The caster in the middle, and its four adjacent tiles marked by who the ability reaches there.
    private fun marks(abilityInfo: AbilityInfo): List<Mark> {
        val healsAllies = abilityInfo.effects.any { effect -> effect is AbilityInfo.Effect.HealAdjacentAllies }
        val (caster, neighbour) =
            when (abilityInfo.targeting) {
                AbilityInfo.Targeting.CASTER -> Mark.ALLY to if (healsAllies) Mark.ALLY else Mark.NONE
                AbilityInfo.Targeting.ADJACENT_ENEMY, AbilityInfo.Targeting.ALL_ADJACENT_ENEMIES -> Mark.CASTER to Mark.ENEMY
                AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_CASTER, AbilityInfo.Targeting.VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT -> Mark.CASTER to Mark.EMPTY_TILE
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
