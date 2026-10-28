package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleInfoView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ArgDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import korlibs.image.color.RGBA
import korlibs.korge.view.View

/**
 * The stories of [BattleInfoView], with the texts of the language global. The announcements that pop when a battle
 * starts or ends, and when a turn starts, are animations: the stories show the lines they leave.
 */
object BattleInfoViewStories : StoryBinding {
    private const val ROUND_LIMIT = 99

    private const val STATE = "state"
    private const val PLAYER = "player"
    private const val ROUND = "round"

    /** What the view displays: exploring, the turn of a player in a round, or the player who won the battle. */
    private enum class State { EXPLORING, TURN, WINNER }

    /** The players of the game, whose turn is in the color of their kind. */
    private enum class Player(
        val id: String,
        val turnColor: RGBA,
    ) {
        HUMAN("player-one", BattleInfoView.HUMAN_TURN_COLOR),
        CPU("player-two", BattleInfoView.CPU_TURN_COLOR),
    }

    private val exploring = StoryDto(id = "screen-battle-info--exploring", name = "Exploring", description = "Out of the battles.", args = mapOf(STATE to ArgDto.text(State.EXPLORING.name)))
    private val humanTurn = StoryDto(id = "screen-battle-info--human-turn", name = "Human turn", description = "The round, and the turn of the human player, in blue.", args = emptyMap())
    private val cpuTurn =
        StoryDto(
            id = "screen-battle-info--cpu-turn",
            name = "CPU turn",
            description = "The round, and the turn of the CPU player, in red.",
            args = mapOf(PLAYER to ArgDto.text(Player.CPU.name), ROUND to ArgDto.int(3)),
        )
    private val winner =
        StoryDto(
            id = "screen-battle-info--winner",
            name = "Winner",
            description = "The player who won the battle, in the middle of the line.",
            args = mapOf(STATE to ArgDto.text(State.WINNER.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-battle-info",
            group = "Screen",
            title = "Battle info",
            description = "On top of the screen: whether the player is exploring, the round and whose turn it is in a battle, or who won it.",
            controls =
                listOf(
                    ControlDto.enum(STATE, State.entries.map { state -> state.name }),
                    ControlDto.enum(PLAYER, Player.entries.map { player -> player.name }),
                    ControlDto.int(ROUND, minimum = 1, maximum = ROUND_LIMIT),
                ),
            args = mapOf(STATE to ArgDto.text(State.TURN.name), PLAYER to ArgDto.text(Player.HUMAN.name), ROUND to ArgDto.int(1)),
            stories = listOf(exploring, humanTurn, cpuTurn, winner),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val player = Player.valueOf(args.text(PLAYER))
        return BattleInfoView(context.texts).apply {
            when (State.valueOf(args.text(STATE))) {
                State.EXPLORING -> displayExploring()
                State.TURN -> displayBattleInfo(playerId = player.id, round = args.int(ROUND), turnColor = player.turnColor)
                State.WINNER -> displayPlayerWin(player.id)
            }
        }
    }
}
