package com.mkz.rpg.screen

import com.mkz.rpg.battle.adapters.presentation.BattleApi
import com.mkz.rpg.player.adapters.presentation.PlayerApi
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHud
import com.mkz.rpg.screen.battlefieldHud.domain.BattlefieldHudRepository
import com.mkz.rpg.shared.adapters.events.InMemoryEventBus
import com.mkz.rpg.shared.domain.EventBus
import korlibs.event.Key
import korlibs.korge.view.addUpdater

private const val LAST_EVENTS_SHOWN = 6

internal actual fun BattleScene.installDebugSupport(
    battleApi: BattleApi,
    playerApi: PlayerApi,
    battlefieldHudRepository: BattlefieldHudRepository,
    eventBus: EventBus,
    effectiveSeed: Long?,
) {
    if (!debugEnabled) return
    val overlay = DebugOverlay()
    sceneView.addChild(overlay)
    var fps = 0.0
    sceneView.addUpdater { frameDelta ->
        val frameMs = frameDelta.inWholeMilliseconds.coerceAtLeast(1).toDouble()
        fps = 0.9 * fps + 0.1 * (1000.0 / frameMs)
        if (keys.justPressed(Key.F3)) {
            overlay.toggle()
            return@addUpdater
        }
        if (!overlay.visible) return@addUpdater
        when {
            keys.justPressed(Key.R) -> restartBattle(effectiveSeed)
            keys.justPressed(Key.N) -> restartBattle(effectiveSeed?.plus(1L))
        }
        updateOverlay(
            overlay,
            fps = fps,
            frameMs = frameMs,
            seed = effectiveSeed,
            battleApi = battleApi,
            playerApi = playerApi,
            battlefieldHudRepository = battlefieldHudRepository,
            inMemoryEventBus = eventBus as? InMemoryEventBus,
        )
    }
}

private fun updateOverlay(
    overlay: DebugOverlay,
    fps: Double,
    frameMs: Double,
    seed: Long?,
    battleApi: BattleApi,
    playerApi: PlayerApi,
    battlefieldHudRepository: BattlefieldHudRepository,
    inMemoryEventBus: InMemoryEventBus?,
) {
    val battle = battleApi.searchBattle()
    val currentPlayer = battle?.let { playerApi.searchPlayerById(it.currentPlayerTurn)?.name }
    val selectedUnitId =
        when (val hud = battlefieldHudRepository.search()) {
            is BattlefieldHud.DisplayMovementRange -> hud.battleUnitId
            is BattlefieldHud.DisplayAbilityCastRange -> hud.battleUnitId
            is BattlefieldHud.DisplayAbilityCastPreview -> hud.battleUnitId
            else -> null
        }
    val lastEvents =
        inMemoryEventBus
            ?.lastEvents
            ?.takeLast(LAST_EVENTS_SHOWN)
            ?.map { it::class.simpleName ?: "event" }
            ?: emptyList()
    overlay.update(
        fps = fps,
        frameMs = frameMs,
        seed = seed,
        round = battle?.currentRound,
        currentPlayer = currentPlayer,
        selectedUnitId = selectedUnitId,
        queueDepth = inMemoryEventBus?.queueDepth ?: 0,
        lastEvents = lastEvents,
    )
}

private fun BattleScene.restartBattle(nextSeed: Long?) {
    injector.root.mapPrototype(BattleScene::class) {
        BattleScene(
            seed = getOrNull<Long>(),
            scenarioPath = getOrNull<String>(),
            debugEnabled = getOrNull<Boolean>() ?: false,
        )
    }
    val injects = (listOfNotNull(nextSeed, scenarioPath) + debugEnabled).toTypedArray()
    sceneContainer.changeToAsync(BattleScene::class, *injects)
}
