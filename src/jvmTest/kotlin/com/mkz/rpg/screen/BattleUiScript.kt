package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.battleUnit.adapters.presentation.BattleUnitApi
import com.mkz.rpg.battlefield.adapters.presentation.BattlefieldApi
import com.mkz.rpg.shared.domain.EventBus
import korlibs.korge.ui.UIButton
import korlibs.korge.view.View
import korlibs.korge.view.descendantsWith
import kotlin.reflect.KClass

/**
 * Drives the battle UI with simulated clicks and exposes battle state through *Api queries,
 * so tests can script player input and assert on the resulting battle state.
 */
class BattleUiScript(
    private val click: suspend (View) -> Unit,
    private val battlefieldView: BattlefieldView,
    private val battleUnitInfoView: BattleUnitInfoView,
    private val playerCallToActionView: PlayerCallToActionView,
    private val battlefieldApi: BattlefieldApi,
    private val battleUnitApi: BattleUnitApi,
    private val battleApi: BattleApi,
    private val eventBus: EventBus,
) {
    fun positionOf(battleUnitId: String) = battlefieldApi.searchPosition(battleUnitId)!!

    fun occupantAt(
        row: Int,
        column: Int,
    ) = battlefieldApi.searchOccupant(row, column)

    fun battleUnit(battleUnitId: String) = battleUnitApi.searchBattleUnitById(battleUnitId)!!

    fun currentPlayerTurn() = battleApi.searchBattle()!!.currentPlayerTurn

    fun castTargets(
        battleUnitId: String,
        abilityId: String,
    ) = battleUnitApi.whereCanCast(battleUnitId = battleUnitId, abilityId = abilityId).flatMap { castGroup -> castGroup.positions }

    fun reachableTiles(battleUnitId: String) = battleUnitApi.whereCanMove(battleUnitId).map { it.row to it.column }.toSet()

    fun isConfirmAndCancelDisplayed() =
        playerCallToActionView.findViewByName("call-to-action")?.let { callToAction ->
            descendants(callToAction, ConfirmButton::class).isNotEmpty() && descendants(callToAction, CancelButton::class).isNotEmpty()
        } ?: false

    suspend fun selectUnit(battleUnitId: String) {
        val position = positionOf(battleUnitId)
        tapTile(row = position.row, column = position.column)
    }

    suspend fun tapTile(
        row: Int,
        column: Int,
    ) {
        click(tileView(row = row, column = column))
        eventBus.dispatch()
    }

    suspend fun selectAbility(abilityIndex: Int) {
        click(abilityButton(index = abilityIndex))
        eventBus.dispatch()
    }

    suspend fun confirmCast() {
        click(actionButton(text = "Confirm"))
        eventBus.dispatch()
    }

    suspend fun cancelCast() {
        click(actionButton(text = "Cancel"))
        eventBus.dispatch()
    }

    suspend fun finishTurn() {
        click(actionButton(text = "Finish turn"))
        eventBus.dispatch()
    }

    private fun actionButton(text: String): UIButton =
        descendants(callToActionView(), UIButton::class)
            .first { it.text == text }

    private fun tileView(
        row: Int,
        column: Int,
    ): View =
        battlefieldView
            .descendantsWith { it.name == "row-$row-column-$column" }
            .firstOrNull()
            ?: error("No tile view at row=$row, column=$column")

    private fun abilityButton(index: Int): AbilityButtonView =
        descendants(battleUnitInfoView, AbilityButtonView::class)
            .getOrNull(index)
            ?: error("No ability button at index $index")

    private fun callToActionView(): View =
        playerCallToActionView.findViewByName("call-to-action")
            ?: error("No call-to-action view is displayed")

    @Suppress("UNCHECKED_CAST")
    private fun <T : View> descendants(
        view: View,
        clazz: KClass<T>,
    ): List<T> =
        view
            .descendantsWith { clazz.isInstance(it) }
            .map { it as T }
}
