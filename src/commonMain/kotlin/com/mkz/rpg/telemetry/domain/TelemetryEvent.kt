package com.mkz.rpg.telemetry.domain

import com.mkz.rpg.shared.domain.DomainEvent

sealed interface TelemetryEvent : DomainEvent {
    object RequestRecordMainMenuOpened : TelemetryEvent

    object RequestStartGame : TelemetryEvent

    data class RequestRecordGameSaved(
        val slot: Int,
    ) : TelemetryEvent

    data class RequestRecordGameLoaded(
        val slot: Int,
    ) : TelemetryEvent

    data class ProductEventRecorded(
        val name: Name,
        val installId: String,
        val sessionId: String,
        val gameId: String?,
        val occurredAt: Double,
        val questId: String?,
        val slot: Int?,
        val lootableItemId: String?,
        val price: Int?,
        val unitId: String?,
    ) : TelemetryEvent {
        enum class Name {
            MAIN_MENU_OPENED,
            GAME_STARTED,
            GAME_LOADED,
            GAME_SAVED,
            QUEST_STARTED,
            QUEST_COMPLETED,
            QUEST_ABANDONED,
            BATTLE_STARTED,
            BATTLE_FINISHED,
            GAME_OVER,
            ITEM_BOUGHT,
            ITEM_SOLD,
            MEMBER_RECRUITED,
        }
    }
}
