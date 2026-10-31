package com.mkz.rpg.party.adapters.save

import com.mkz.rpg.party.domain.Party
import com.mkz.rpg.party.usecases.commands.RestoreParty
import com.mkz.rpg.party.usecases.queries.SearchPartySnapshot
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/**
 * The party exploring: its members and its line. What it was doing (e.g.: walking) is not saved, as the game is only
 * saved while exploring, done walking: it explores again once restored, see [RestoreParty].
 */
class PartySnapshottable(
    private val searchPartySnapshot: SearchPartySnapshot,
    private val restoreParty: RestoreParty,
) : Snapshottable {
    override val key = "party"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchPartySnapshot()?.let { party ->
                PartySnapshot(playerId = party.playerId, battleUnitIds = party.battleUnitIds, lineBattleUnitIds = party.lineBattleUnitIds)
            },
        )

    override fun restore(snapshot: String) {
        val party = SnapshotJson.decodeFromString<PartySnapshot?>(snapshot)
        restoreParty(party?.let { Party.Dto.SnapshotDto(playerId = it.playerId, battleUnitIds = it.battleUnitIds, lineBattleUnitIds = it.lineBattleUnitIds) })
    }
}

@Serializable
private data class PartySnapshot(
    val playerId: String,
    val battleUnitIds: List<String>,
    val lineBattleUnitIds: List<String>,
)
