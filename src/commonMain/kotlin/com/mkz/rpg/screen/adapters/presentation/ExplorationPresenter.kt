package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyEvent.ExplorationPaused
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkStarted
import com.mkz.rpg.screen.adapters.presentation.ShopView.Mode
import com.mkz.rpg.screen.adapters.presentation.battlefieldHud.domain.BattlefieldHudEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Walks the party with the virtual pad while exploring. The pad takes the place of the battle HUD and the player call
 * to action, which are displayed while battling, and while exploring when a battle unit of the party is selected (e.g.:
 * to use its inventory) or a found item does not fit in the inventory. It also counts the seconds while exploring, for the terrain
 * effects of the battle units standing still. Once the game is over, the pad is hidden and the seconds are no longer
 * counted.
 *
 * A press while the scene is still playing (e.g.: a battle unit flashing with its terrain effect) is not lost: the last
 * arrow or A pressed is acted on once the scene is idle, unless the pad is hidden meanwhile.
 *
 * Pressing A talks to the npc next to the leader: its dialog takes the place of the pad until it is tapped. Time stops
 * meanwhile, as the party neither walks nor starts a second while talking. The dialog of a merchant is not closed by
 * tapping it, but by its Leave choice. Its Buy and Sell choices open its shop in place of the dialog (see
 * [ShopPresenter]), until Back displays the dialog again.
 */
class ExplorationPresenter(
    private val virtualPadView: VirtualPadView,
    private val npcDialogView: NpcDialogView,
    private val battleHudView: BattleHudView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val partyApi: PartyApi,
    private val npcApi: NpcApi,
    private val shopPresenter: ShopPresenter,
    private val timeline: BattleSceneTimeline,
    eventBus: EventBus,
) {
    companion object {
        // How often the battle units standing still receive the effect of their terrain while exploring.
        val STANDING_TERRAIN_EFFECT_INTERVAL = 1.seconds
    }

    // The last arrow or A pressed, until it is acted on.
    private sealed interface Press {
        data class Arrow(
            val direction: DirectionDto,
        ) : Press

        data object A : Press
    }

    private var elapsedSinceTerrainEffects = Duration.ZERO
    private var isGameOver = false
    private var pendingPress: Press? = null

    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<ExplorationPaused> {
                timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            // While exploring, selecting a battle unit of the party displays its info and inventory in place of the pad,
            // until the HUD goes idle (e.g.: an empty tile is selected).
            eventBus.subscribe<BattlefieldHudEvent.SelectedBattleUnit> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<BattlefieldHudEvent.Idle> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            // Straight away, as the timeline waits until the player chooses what to do with the found item.
            eventBus.subscribe<InventoryFull> {
                if (isExploring()) displayVirtualPad(isDisplayed = false)
            },
            eventBus.subscribe<NpcTalkStarted> { event ->
                val npc = npcApi.searchNpcById(event.npcId) ?: return@subscribe
                timeline.play { displayNpcDialog(npc) }
            },
            eventBus.subscribe<NpcTalkFinished> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<PlayerVictory> {
                isGameOver = true
                timeline.play {
                    virtualPadView.visible = false
                    npcDialogView.visible = false
                    shopPresenter.close()
                }
            },
        )

    init {
        virtualPadView.visible = false
        npcDialogView.visible = false
        virtualPadView.setOnDirectionPressed { direction -> pendingPress = Press.Arrow(direction) }
        virtualPadView.setOnDirectionHeld { direction -> walkParty(direction) }
        virtualPadView.setOnAPressed {
            pendingPress = Press.A
            talkToAdjacentNpc()
        }
        npcDialogView.setOnTapped { dialogTapped() }
        shopPresenter.setOnBack { shopBackSelected() }
    }

    /**
     * Acts on the press pending since the scene was playing, counts the time elapsed since the previous frame, and applies
     * the standing terrain effects every second.
     */
    fun tick(elapsed: Duration) {
        if (isGameOver) return
        actOnPendingPress()
        elapsedSinceTerrainEffects += elapsed
        while (elapsedSinceTerrainEffects >= STANDING_TERRAIN_EFFECT_INTERVAL) {
            elapsedSinceTerrainEffects -= STANDING_TERRAIN_EFFECT_INTERVAL
            partyApi.applyStandingTerrainEffects()
        }
    }

    fun dispose() {
        subscriptions.forEach(Subscription::dispose)
    }

    // A press is kept while the scene plays, and forgotten once the pad is hidden (e.g.: a battle starts).
    private fun actOnPendingPress() {
        if (!virtualPadView.visible) pendingPress = null
        when (val press = pendingPress) {
            is Press.Arrow -> walkParty(press.direction)
            Press.A -> talkToAdjacentNpc()
            null -> Unit
        }
    }

    // The party only walks once the scene shows where it is, so a held arrow can not walk ahead of the animation, nor
    // while the pad is hidden (e.g.: an arrow still held when the inventory is displayed). Walking towards the pressed
    // arrow acts on its press, so it is not walked again.
    private fun walkParty(direction: DirectionDto) {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.Arrow(direction)) pendingPress = null
        partyApi.walkParty(direction)
    }

    // As walking: once the scene shows where the party is, and while the pad is displayed.
    private fun talkToAdjacentNpc() {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.A) pendingPress = null
        npcApi.talkToAdjacentNpc()
    }

    // Finishes the talk, but with a merchant, which is left with its Leave choice.
    private fun dialogTapped() {
        if (timeline.isPlaying || !npcDialogView.visible || talkingNpc()?.isMerchant() == true) return
        partyApi.finishTalk()
    }

    // The merchant's Buy or Sell choice.
    private fun openShop(mode: Mode) {
        if (timeline.isPlaying || !npcDialogView.visible) return
        val merchant = talkingNpc() ?: return
        timeline.play {
            npcDialogView.visible = false
            shopPresenter.open(merchant, mode)
        }
    }

    // The merchant's Leave choice.
    private fun leaveMerchant() {
        if (timeline.isPlaying || !npcDialogView.visible) return
        partyApi.finishTalk()
    }

    // The party keeps talking to the merchant.
    private fun shopBackSelected() {
        val merchant = talkingNpc() ?: return
        timeline.play {
            shopPresenter.close()
            displayNpcDialog(merchant)
        }
    }

    private fun talkingNpc(): Npc.Dto? = partyApi.searchParty()?.talkingNpcId?.let { npcId -> npcApi.searchNpcById(npcId) }

    private fun Npc.Dto.isMerchant() = stock.isNotEmpty()

    private fun isExploring() = !isGameOver && partyApi.searchParty()?.isExploring == true

    // The pad takes the place of the battle HUD and the call to action, or the other way around. Either closes the dialog
    // and the shop.
    private fun displayVirtualPad(isDisplayed: Boolean) {
        npcDialogView.visible = false
        shopPresenter.close()
        virtualPadView.visible = isDisplayed
        battleHudView.visible = !isDisplayed
        playerCallToActionView.visible = !isDisplayed
    }

    // The dialog takes the place of the pad, with Buy, Sell and Leave for a merchant.
    private fun displayNpcDialog(npc: Npc.Dto) {
        val choices =
            if (npc.isMerchant()) {
                listOf(
                    NpcDialogView.Choice("Buy") { openShop(Mode.BUY) },
                    NpcDialogView.Choice("Sell") { openShop(Mode.SELL) },
                    NpcDialogView.Choice("Leave") { leaveMerchant() },
                )
            } else {
                emptyList()
            }
        npcDialogView.display(npc, choices)
        virtualPadView.visible = false
        npcDialogView.visible = true
    }
}
