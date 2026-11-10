package com.mkz.rpg.battlefield.domain

import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.shared.domain.DomainEvent

sealed interface BattlefieldEvent : DomainEvent {
    object BattlefieldCreated : BattlefieldEvent

    object RequestInitialiseBattlefield : BattlefieldEvent

    data class RequestTerrainEffects(
        val occupantIds: Set<String>,
    ) : BattlefieldEvent

    data class RequestOccupyTile(
        val occupantId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent

    data class RequestOccupyTiles(
        val occupantId: String,
        val row: Int,
        val column: Int,
        val rows: Int,
        val columns: Int,
    ) : BattlefieldEvent

    data class RequestMoveOccupant(
        val occupantId: String,
        val toRow: Int,
        val toColumn: Int,
    ) : BattlefieldEvent

    data class RequestSwapOccupants(
        val occupantId: String,
        val otherOccupantId: String,
    ) : BattlefieldEvent

    data class RequestRemoveOccupant(
        val occupantId: String,
    ) : BattlefieldEvent

    data class TerrainEffectApplied(
        val application: EffectApplicationDto,
    ) : BattlefieldEvent

    data class TileOccupied(
        val row: Int,
        val column: Int,
        val occupantId: String,
    ) : BattlefieldEvent

    data class OccupantRemoved(
        val occupantId: String,
        val row: Int,
        val column: Int,
    ) : BattlefieldEvent
}
