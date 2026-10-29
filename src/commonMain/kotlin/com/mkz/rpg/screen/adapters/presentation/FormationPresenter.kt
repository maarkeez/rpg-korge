package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.party.adapters.presentation.PartyApi

/**
 * The party formation, where the player sets the party order while exploring: its first member leads. It opens with
 * the party in its order, and every tile dimmed but the party's. Tapping a member selects it, framing its row and its
 * tile, and tapping another one swaps both, clearing the selection, as tapping the selected one again does. Close
 * applies the order displayed to the party, which explores again.
 */
class FormationPresenter(
    private val formationView: FormationView,
    private val battlefieldView: BattlefieldView,
    private val partyApi: PartyApi,
    private val battleUnitApi: BattleUnitApi,
    private val battlefieldApi: BattlefieldApi,
    private val texts: Texts,
    private val timeline: BattleSceneTimeline,
) : FormationView.Delegate {
    // What the scene displays. Changed in timeline order, like the views.
    private var order: List<String> = emptyList()
    private var selectedIndex: Int? = null

    /** Whether the scene displays the formation. */
    var isOpen = false
        private set

    init {
        formationView.visible = false
        formationView.setDelegate(this)
    }

    /** Displays the party in its order, the leader first. To be played in the timeline. */
    fun open() {
        order = partyApi.searchParty()?.battleUnitIds.orEmpty()
        isOpen = true
        displayFormation()
        formationView.visible = true
    }

    /** Hides the formation and restores the tiles. Nothing happens when it is not open. */
    fun close() {
        if (!isOpen) return
        isOpen = false
        order = emptyList()
        selectedIndex = null
        formationView.visible = false
        battlefieldView.resetTiles()
    }

    // Displays the party in the order set so far, the crown on its first member, and clears the selection.
    private fun displayFormation() {
        val rows =
            order.mapIndexedNotNull { index, battleUnitId ->
                val battleUnit = battleUnitApi.searchBattleUnitById(battleUnitId) ?: return@mapIndexedNotNull null
                FormationView.Row(
                    unitId = battleUnit.unitId,
                    name = texts.unitName(battleUnit.unitId),
                    remainingHealth = battleUnit.remainingHealthPoints,
                    maximumHealth = battleUnit.maximumHealthPoints,
                    remainingMana = battleUnit.remainingManaPoints,
                    maximumMana = battleUnit.maximumManaPoints,
                    isLeader = index == 0,
                )
            }
        formationView.display(rows)
        select(null)
    }

    // Frames the selected member's row and tile, every tile but the party's dimmed, with the hint for the next tap.
    private fun select(index: Int?) {
        selectedIndex = index
        formationView.selectRow(index)
        val selectedId = index?.let(order::getOrNull)
        formationView.displayHint(
            if (selectedId == null) {
                texts[UiText.FORMATION_TAP_TO_SELECT]
            } else {
                texts[UiText.FORMATION_TAP_TO_SWAP, "name" to memberName(selectedId)]
            },
        )
        battlefieldView.resetTiles()
        battlefieldView.dimTilesExcept(BattlefieldMapper.positions(order.mapNotNull(battlefieldApi.searchPosition::invoke)))
        selectedId?.let(battlefieldApi.searchPosition::invoke)?.let { position -> battlefieldView.displayTileSelection(position.row, position.column) }
    }

    private fun memberName(battleUnitId: String): String = battleUnitApi.searchBattleUnitById(battleUnitId)?.let { battleUnit -> texts.unitName(battleUnit.unitId) }.orEmpty()

    // Selects the member, or swaps it with the selected one, or clears the selection when it is the selected one.
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

    // The player can only act once the scene shows the current state of the formation.
    private fun whenIdle(action: () -> Unit) {
        if (!timeline.isPlaying && isOpen) action()
    }

    // Delegates
    override fun formationRowSelected(index: Int) = whenIdle { timeline.play { tapMember(index) } }

    override fun formationCloseSelected() = whenIdle { partyApi.closeFormation(order) }
}
