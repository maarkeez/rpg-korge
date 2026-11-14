package com.mkz.tessavar.quest.adapters.save

import com.mkz.tessavar.quest.domain.Quest.Dto.ProgressDto
import com.mkz.tessavar.quest.domain.Quest.Dto.StatusDto
import com.mkz.tessavar.quest.usecases.commands.LoadQuestsSnapshot
import com.mkz.tessavar.quest.usecases.queries.SearchQuestsSnapshot
import com.mkz.tessavar.shared.adapters.save.SnapshotJson
import com.mkz.tessavar.shared.domain.Snapshottable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

class QuestSnapshottable(
    private val searchQuestsSnapshot: SearchQuestsSnapshot,
    private val loadQuestsSnapshot: LoadQuestsSnapshot,
) : Snapshottable {
    override val key = "quest"

    override fun snapshot(): String =
        SnapshotJson.encodeToString(
            searchQuestsSnapshot().map { progress ->
                QuestSnapshot(
                    questId = progress.questId,
                    status = progress.status.name,
                    currentTaskIndex = progress.currentTaskIndex,
                    currentTaskDefeats = progress.currentTaskDefeats,
                    isPinned = progress.isPinned,
                )
            },
        )

    override fun load(snapshot: String) =
        loadQuestsSnapshot(
            SnapshotJson.decodeFromString<List<QuestSnapshot>>(snapshot).map { quest ->
                ProgressDto(
                    questId = quest.questId,
                    status = enumValueOf<StatusDto>(quest.status),
                    currentTaskIndex = quest.currentTaskIndex,
                    currentTaskDefeats = quest.currentTaskDefeats,
                    isPinned = quest.isPinned,
                )
            },
        )
}

@Serializable
private data class QuestSnapshot(
    val questId: String,
    val status: String,
    val currentTaskIndex: Int,
    val currentTaskDefeats: Int,
    val isPinned: Boolean,
)
