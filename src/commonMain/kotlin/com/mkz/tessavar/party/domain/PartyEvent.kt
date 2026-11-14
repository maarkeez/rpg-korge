package com.mkz.tessavar.party.domain

import com.mkz.tessavar.shared.domain.DomainEvent

sealed interface PartyEvent : DomainEvent {
    object RequestStartExploration : PartyEvent

    data class ExplorationStarted(
        val playerId: String,
        val battleUnitIds: List<String>,
        val unitIds: List<String>,
    ) : PartyEvent

    data class PartyStepped(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : PartyEvent

    data class ExplorationInterrupted(
        val playerId: String,
    ) : PartyEvent

    data class ExplorationResumed(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : PartyEvent

    data class GameOver(
        val playerId: String,
    ) : PartyEvent

    data class ExplorationSecondStarted(
        val playerId: String,
    ) : PartyEvent

    data class RequestStartTalk(
        val npcId: String,
    ) : PartyEvent

    data class NpcTalkStarted(
        val playerId: String,
        val npcId: String,
    ) : PartyEvent

    data class NpcTalkFinished(
        val playerId: String,
        val npcId: String,
    ) : PartyEvent

    data class FormationOpened(
        val playerId: String,
    ) : PartyEvent

    data class FormationClosed(
        val playerId: String,
        val battleUnitIds: List<String>,
    ) : PartyEvent

    data class PauseOpened(
        val playerId: String,
    ) : PartyEvent

    data class PauseClosed(
        val playerId: String,
    ) : PartyEvent

    data class QuestOpened(
        val playerId: String,
    ) : PartyEvent

    data class QuestClosed(
        val playerId: String,
    ) : PartyEvent

    data class RequestRecruitMember(
        val battleUnitId: String,
        val unitId: String,
        val row: Int,
        val column: Int,
    ) : PartyEvent

    data class RequestJoinParty(
        val battleUnitId: String,
    ) : PartyEvent

    data class PartyJoined(
        val playerId: String,
        val battleUnitId: String,
        val unitId: String,
    ) : PartyEvent

    data class RequestApplyMemberEffect(
        val battleUnitId: String,
        val lootableItemId: String,
        val effectId: String,
    ) : PartyEvent

    data class RequestRemoveMemberEquippedEffects(
        val battleUnitId: String,
        val lootableItemId: String,
    ) : PartyEvent
}
