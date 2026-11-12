package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BannerView
import com.mkz.rpg.storybook.domain.Storybook.Dto.ComponentDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ControlDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.StoryDto
import com.mkz.rpg.storybook.domain.Storybook.Dto.ValueDto
import korlibs.image.color.RGBA
import korlibs.korge.view.View

object BannerViewStories : StoryBinding {
    private const val ROUND_LIMIT = 99

    private const val STATE = "state"
    private const val PLAYER = "player"
    private const val ROUND = "round"

    private enum class State { EXPLORING, TURN, GAME_OVER }

    private enum class Player(
        val id: String,
        val turnColor: RGBA,
    ) {
        HUMAN("player-one", BannerView.HUMAN_TURN_COLOR),
        CPU("player-two", BannerView.CPU_TURN_COLOR),
    }

    private val exploring = StoryDto(id = "screen-banner--exploring", name = "Exploring", description = "Out of the battles.", args = mapOf(STATE to ValueDto.text(State.EXPLORING.name)))
    private val humanTurn = StoryDto(id = "screen-banner--human-turn", name = "Human turn", description = "The round, and the turn of the human player, in blue.", args = emptyMap())
    private val cpuTurn =
        StoryDto(
            id = "screen-banner--cpu-turn",
            name = "CPU turn",
            description = "The round, and the turn of the CPU player, in red.",
            args = mapOf(PLAYER to ValueDto.text(Player.CPU.name), ROUND to ValueDto.int(3)),
        )
    private val gameOver =
        StoryDto(
            id = "screen-banner--game-over",
            name = "Game over",
            description = "The game over, in the middle of the line.",
            args = mapOf(STATE to ValueDto.text(State.GAME_OVER.name)),
        )

    override fun component(context: StoryContext) =
        ComponentDto(
            id = "screen-banner",
            group = "Screen",
            title = "Banner",
            description = "On top of the screen: whether the player is exploring, the round and whose turn it is in a battle, or the game over.",
            controls =
                listOf(
                    ControlDto.enum(STATE, State.entries.map { state -> state.name }),
                    ControlDto.enum(PLAYER, Player.entries.map { player -> player.name }),
                    ControlDto.int(ROUND, minimum = 1, maximum = ROUND_LIMIT),
                ),
            args = mapOf(STATE to ValueDto.text(State.TURN.name), PLAYER to ValueDto.text(Player.HUMAN.name), ROUND to ValueDto.int(1)),
            stories = listOf(exploring, humanTurn, cpuTurn, gameOver),
        )

    override fun render(
        args: Args,
        context: StoryContext,
    ): View {
        val player = Player.valueOf(args.text(PLAYER))
        return BannerView(context.messages).apply {
            when (State.valueOf(args.text(STATE))) {
                State.EXPLORING -> displayExploring()
                State.TURN -> displayBanner(playerId = player.id, round = args.int(ROUND), turnColor = player.turnColor)
                State.GAME_OVER -> displayGameOver()
            }
        }
    }
}
