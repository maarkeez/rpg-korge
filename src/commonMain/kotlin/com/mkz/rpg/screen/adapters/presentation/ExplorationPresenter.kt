package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battle.domain.BattleEvent.BattleStarted
import com.mkz.rpg.battle.domain.BattleEvent.PlayerVictory
import com.mkz.rpg.inventory.domain.InventoryEvent.InventoryFull
import com.mkz.rpg.npc.adapters.presentation.NpcApi
import com.mkz.rpg.npc.domain.Npc
import com.mkz.rpg.party.adapters.presentation.PartyApi
import com.mkz.rpg.party.domain.Party.Dto.DirectionDto
import com.mkz.rpg.party.domain.PartyEvent.ExplorationInterrupted
import com.mkz.rpg.party.domain.PartyEvent.ExplorationResumed
import com.mkz.rpg.party.domain.PartyEvent.ExplorationStarted
import com.mkz.rpg.party.domain.PartyEvent.FormationClosed
import com.mkz.rpg.party.domain.PartyEvent.FormationOpened
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkFinished
import com.mkz.rpg.party.domain.PartyEvent.NpcTalkStarted
import com.mkz.rpg.party.domain.PartyEvent.PauseClosed
import com.mkz.rpg.party.domain.PartyEvent.PauseOpened
import com.mkz.rpg.party.domain.PartyEvent.QuestClosed
import com.mkz.rpg.party.domain.PartyEvent.QuestOpened
import com.mkz.rpg.save.domain.SaveEvent.GameLoaded
import com.mkz.rpg.screen.adapters.presentation.ShopView.Mode
import com.mkz.rpg.screen.adapters.presentation.selection.domain.SelectionEvent
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.shared.domain.subscribe
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ExplorationPresenter(
    private val virtualPadView: VirtualPadView,
    private val talkView: TalkView,
    private val hudView: HudView,
    private val callToActionView: CallToActionView,
    private val partyApi: PartyApi,
    private val npcApi: NpcApi,
    private val shopPresenter: ShopPresenter,
    private val formationPresenter: FormationPresenter,
    private val pausePresenter: PausePresenter,
    private val questPresenter: QuestPresenter,
    private val messages: Messages,
    private val timeline: GameSceneTimeline,
    eventBus: EventBus,
) {
    companion object {
        val STANDING_TERRAIN_EFFECT_INTERVAL = 1.seconds
    }

    private sealed interface Press {
        data class Arrow(
            val direction: VirtualPadView.Direction,
        ) : Press

        data object A : Press

        data object B : Press

        data object Start : Press
    }

    private var elapsedSinceTerrainEffects = Duration.ZERO
    private var isGameOver = false
    private var pendingPress: Press? = null

    private val subscriptions =
        listOf(
            eventBus.subscribe<ExplorationStarted> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<GameLoaded> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<BattleStarted> {
                if (!isGameOver) timeline.playNext { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<ExplorationInterrupted> {
                timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<ExplorationResumed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<SelectionEvent.BattleUnitSelected> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = false) }
            },
            eventBus.subscribe<SelectionEvent.Deselected> {
                if (isExploring()) timeline.play { displayVirtualPad(isDisplayed = true) }
            },
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
            eventBus.subscribe<FormationOpened> {
                timeline.play {
                    virtualPadView.visible = false
                    formationPresenter.open()
                }
            },
            eventBus.subscribe<FormationClosed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<PauseOpened> {
                timeline.play {
                    virtualPadView.visible = false
                    pausePresenter.open()
                }
            },
            eventBus.subscribe<PauseClosed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<QuestOpened> {
                timeline.play {
                    virtualPadView.visible = false
                    questPresenter.openQuest()
                }
            },
            eventBus.subscribe<QuestClosed> {
                timeline.play { displayVirtualPad(isDisplayed = true) }
            },
            eventBus.subscribe<PlayerVictory> {
                isGameOver = true
                timeline.play {
                    virtualPadView.visible = false
                    talkView.visible = false
                    shopPresenter.close()
                    formationPresenter.close()
                    pausePresenter.close()
                    questPresenter.closeQuest()
                    callToActionView.hideButtons()
                }
            },
        )

    init {
        virtualPadView.visible = false
        talkView.visible = false
        virtualPadView.setOnDirectionPressed { direction -> pendingPress = Press.Arrow(direction) }
        virtualPadView.setOnDirectionHeld { direction -> stepParty(direction) }
        virtualPadView.setOnAPressed {
            pendingPress = Press.A
            talkToAdjacentNpc()
        }
        virtualPadView.setOnBPressed {
            pendingPress = Press.B
            openFormation()
        }
        virtualPadView.setOnStartPressed {
            pendingPress = Press.Start
            openPause()
        }
        talkView.setOnTapped { talkPanelTapped() }
        shopPresenter.setOnBack { shopBackSelected() }
        questPresenter.setOnTrackerSelected { openQuest() }
    }

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

    private fun actOnPendingPress() {
        if (!virtualPadView.visible) pendingPress = null
        when (val press = pendingPress) {
            is Press.Arrow -> stepParty(press.direction)
            Press.A -> talkToAdjacentNpc()
            Press.B -> openFormation()
            Press.Start -> openPause()
            null -> Unit
        }
    }

    private fun stepParty(direction: VirtualPadView.Direction) {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.Arrow(direction)) pendingPress = null
        partyApi.stepParty(directionDto(direction))
    }

    private fun directionDto(direction: VirtualPadView.Direction): DirectionDto =
        when (direction) {
            VirtualPadView.Direction.NORTH -> DirectionDto.NORTH
            VirtualPadView.Direction.EAST -> DirectionDto.EAST
            VirtualPadView.Direction.SOUTH -> DirectionDto.SOUTH
            VirtualPadView.Direction.WEST -> DirectionDto.WEST
        }

    private fun talkToAdjacentNpc() {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.A) pendingPress = null
        npcApi.talkToAdjacentNpc()
    }

    private fun openFormation() {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.B) pendingPress = null
        partyApi.openFormation()
    }

    private fun openPause() {
        if (timeline.isPlaying || !virtualPadView.visible) return
        if (pendingPress == Press.Start) pendingPress = null
        partyApi.openPause()
    }

    private fun openQuest() {
        if (timeline.isPlaying || !virtualPadView.visible) return
        partyApi.openQuest()
    }

    private fun talkPanelTapped() {
        if (timeline.isPlaying || !talkView.visible || talkingNpc()?.let { npc -> npcApi.searchIsMerchant(npc.id) } == true) return
        partyApi.finishTalk()
    }

    private fun openShop(mode: Mode) {
        if (timeline.isPlaying || !talkView.visible) return
        val merchant = talkingNpc() ?: return
        timeline.play {
            talkView.visible = false
            shopPresenter.open(merchant, mode)
        }
    }

    private fun leaveMerchant() {
        if (timeline.isPlaying || !talkView.visible) return
        partyApi.finishTalk()
    }

    private fun shopBackSelected() {
        val merchant = talkingNpc() ?: return
        timeline.play {
            shopPresenter.close()
            displayNpcDialog(merchant)
        }
    }

    private fun talkingNpc(): Npc.Dto? = partyApi.searchParty()?.talkingNpcId?.let { npcId -> npcApi.searchNpcById(npcId) }

    private fun isExploring() = !isGameOver && partyApi.searchParty()?.isExploring == true

    private fun displayVirtualPad(isDisplayed: Boolean) {
        talkView.visible = false
        shopPresenter.close()
        formationPresenter.close()
        pausePresenter.close()
        questPresenter.closeQuest()
        callToActionView.hideButtons()
        virtualPadView.visible = isDisplayed
        hudView.visible = !isDisplayed
        callToActionView.visible = !isDisplayed
    }

    private fun displayNpcDialog(npc: Npc.Dto) {
        talkView.display(TalkInfo(npcId = npc.id, unitId = npc.unitId, name = messages.npcName(npc.id), line = messages.npcLine(npc.id)))
        virtualPadView.visible = false
        talkView.visible = true
        if (npcApi.searchIsMerchant(npc.id)) {
            callToActionView.displayButtons(
                listOf(
                    ButtonRowView.Action(messages[UiMessage.TALK_LEAVE]) { leaveMerchant() },
                    ButtonRowView.Action(messages[UiMessage.TALK_SELL]) { openShop(Mode.SELL) },
                    ButtonRowView.Action(messages[UiMessage.TALK_BUY], ConfirmButton.COLOR, ConfirmButton.PRESSED_COLOR) { openShop(Mode.BUY) },
                ),
            )
        }
    }
}
