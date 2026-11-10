package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.party.adapters.presentation.PartyApi

class FormationPresenter(
    private val formationView: FormationView,
    private val battlefieldView: BattlefieldView,
    private val callToActionView: CallToActionView,
    private val partyApi: PartyApi,
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldApi: BattlefieldApi,
    private val messages: Messages,
    private val timeline: GameSceneTimeline,
) : FormationView.Delegate {
    private var order: List<String> = emptyList()
    private var selectedIndex: Int? = null

    var isOpen = false
        private set

    init {
        formationView.visible = false
        formationView.setDelegate(this)
    }

    fun open() {
        order = partyApi.searchParty()?.battleUnitIds.orEmpty()
        isOpen = true
        displayFormation()
        formationView.visible = true
        callToActionView.displayButtons(listOf(ButtonRowView.Action(messages[UiMessage.FORMATION_CLOSE]) { whenIdle { partyApi.closeFormation(order) } }))
    }

    fun close() {
        if (!isOpen) return
        isOpen = false
        order = emptyList()
        selectedIndex = null
        formationView.visible = false
        battlefieldView.resetTiles()
    }

    private fun displayFormation() {
        val rows =
            order.mapIndexedNotNull { index, battleUnitId ->
                val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return@mapIndexedNotNull null
                FormationView.Row(
                    unitId = battleUnit.unitId,
                    name = messages.unitName(battleUnit.unitId),
                    remainingHealth = battleUnit.remainingHealth,
                    maximumHealth = battleUnit.maximumHealth,
                    remainingMana = battleUnit.remainingMana,
                    maximumMana = battleUnit.maximumMana,
                    isLeader = index == 0,
                )
            }
        formationView.display(rows)
        select(null)
    }

    private fun select(index: Int?) {
        selectedIndex = index
        formationView.selectRow(index)
        val selectedId = index?.let(order::getOrNull)
        formationView.displayHint(
            if (selectedId == null) {
                messages[UiMessage.FORMATION_TAP_TO_SELECT]
            } else {
                messages[UiMessage.FORMATION_TAP_TO_REORDER, "name" to memberName(selectedId)]
            },
        )
        battlefieldView.resetTiles()
        battlefieldView.dimTilesExcept(BattlefieldMapper.positions(order.mapNotNull(battlefieldApi.searchPosition::invoke)))
        selectedId?.let(battlefieldApi.searchPosition::invoke)?.let { position -> battlefieldView.displayTileSelection(position.row, position.column) }
    }

    private fun memberName(battleUnitId: String): String = battleUnitApi.searchBattleUnitById(battleUnitId)?.let { battleUnit -> messages.unitName(battleUnit.unitId) }.orEmpty()

    private fun tapMember(index: Int) {
        if (index !in order.indices) return
        val selected = selectedIndex
        when (selected) {
            null -> select(index)
            index -> select(null)
            else -> {
                order =
                    order.toMutableList().apply {
                        this[selected] = order[index]
                        this[index] = order[selected]
                    }
                displayFormation()
            }
        }
    }

    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && isOpen) action()
    }

    override fun formationRowSelected(index: Int) = whenIdle { timeline.play { tapMember(index) } }
}
