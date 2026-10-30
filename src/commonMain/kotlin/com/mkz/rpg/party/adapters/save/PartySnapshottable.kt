package com.mkz.rpg.party.adapters.save

import com.mkz.rpg.battleUnit.usecases.queries.SearchBattleUnitById
import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable

/**
 * The party exploring: its members and its line. What it was doing (e.g.: walking) is not saved, as the game is only
 * saved while exploring, done walking: it explores again once restored. Restoring fails when a member is not a battle
 * unit, so the battle units are restored before.
 */
class PartySnapshottable(
    private val partyRepository: PartyRepository,
    private val searchBattleUnitById: SearchBattleUnitById,
) : Snapshottable {
    override val key = "party"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            partyRepository.search()?.toSnapshot()?.let { party ->
                PartySnapshot(playerId = party.playerId, battleUnitIds = party.battleUnitIds, lineBattleUnitIds = party.lineBattleUnitIds)
            },
        )

    override fun restore(snapshot: String) {
        val party = SnapshotJson.decodeFromString<PartySnapshot?>(snapshot) ?: return
        party.battleUnitIds.forEach { battleUnitId -> requireNotNull(searchBattleUnitById(battleUnitId)) { "Unknown battle unit $battleUnitId" } }
        partyRepository.save(Party.restore(Party.Dto.SnapshotDto(playerId = party.playerId, battleUnitIds = party.battleUnitIds, lineBattleUnitIds = party.lineBattleUnitIds)))
    }
}

@Serializable
private data class PartySnapshot(
    val playerId: String,
    val battleUnitIds: List<String>,
    val lineBattleUnitIds: List<String>,
)
