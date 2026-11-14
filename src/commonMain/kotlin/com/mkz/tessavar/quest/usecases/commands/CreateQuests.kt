package com.mkz.tessavar.quest.usecases.commands

import com.mkz.tessavar.battlefield.usecases.queries.IsTileVacant
import com.mkz.tessavar.quest.domain.Quest
import com.mkz.tessavar.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.tessavar.quest.domain.Quest.Dto.TaskDto
import com.mkz.tessavar.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.tessavar.quest.domain.QuestError.QuestTileNotVacant
import com.mkz.tessavar.quest.domain.QuestRepository
import com.mkz.tessavar.quest.usecases.services.QuestLoader

class CreateQuests(
    private val questLoader: QuestLoader,
    private val questRepository: QuestRepository,
    private val isTileVacant: IsTileVacant,
) {
    operator fun invoke() {
        val quests = questLoader.loadQuests()
        if (quests.count { quest -> quest.type == QuestTypeDto.MAIN } != 1) throw MainQuestNotUnique()
        quests.forEach { questDto ->
            if (questRepository.searchById(questDto.id) != null) return@forEach
            val tiles = questDto.startTiles + questDto.tasks.mapNotNull(TaskDto::visit).flatMap(TaskDto.VisitDto::tiles)
            if (tiles.any { tile -> !isTileVacant(tile.row, tile.column) }) throw QuestTileNotVacant()
            questRepository.save(Quest.create(questDto))
        }
    }
}
