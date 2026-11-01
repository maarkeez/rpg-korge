package com.mkz.rpg.quest.adapters.save

import com.mkz.rpg.quest.domain.Quest.Dto.ProgressDto
import com.mkz.rpg.quest.domain.Quest.Dto.StatusDto
import com.mkz.rpg.quest.usecases.commands.RestoreQuests
import com.mkz.rpg.quest.usecases.queries.SearchQuestsSnapshot
import com.mkz.rpg.shared.adapters.save.SnapshotJson
import com.mkz.rpg.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

/** How far each quest went, see [SearchQuestsSnapshot] and [RestoreQuests]. */
class QuestSnapshottable(
    private val searchQuestsSnapshot: SearchQuestsSnapshot,
    private val restoreQuests: RestoreQuests,
) : Snapshottable {
    override val key = "quest"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchQuestsSnapshot().map { progress ->
                QuestSnapshot(
                    questId = progress.questId,
                    status = progress.status.name,
                    currentTaskIndex = progress.currentTaskIndex,
                    currentTaskProgress = progress.currentTaskProgress,
                )
            },
        )

    override fun restore(snapshot: String) =
        restoreQuests(
            SnapshotJson.decodeFromString<List<QuestSnapshot>>(snapshot).map { quest ->
                ProgressDto(
                    questId = quest.questId,
                    status = enumValueOf<StatusDto>(quest.status),
                    currentTaskIndex = quest.currentTaskIndex,
                    currentTaskProgress = quest.currentTaskProgress,
                )
            },
        )
}

@Serializable
private data class QuestSnapshot(
    val questId: String,
    val status: String,
    val currentTaskIndex: Int,
    val currentTaskProgress: Int,
)
