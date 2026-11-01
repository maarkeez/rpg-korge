package com.mkz.rpg.quest.usecases.commands

import com.mkz.rpg.battlefield.usecases.queries.CanBattlefieldTileBeOccupied
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.quest.domain.QuestError.MainQuestNotUnique
import com.mkz.rpg.quest.domain.QuestError.QuestTileCanNotBeOccupied
import com.mkz.rpg.quest.domain.QuestRepository
import com.mkz.rpg.quest.usecases.services.QuestLoader

/**
 * Creates every loaded quest, unless it already exists, failing unless exactly one is the main quest, or when a tile to reach can not be occupied
 * (e.g.: void, out of the battlefield). The units, npcs and lootable items they name are checked by `QuestResourcesTest` instead.
 */
class InitialiseQuests(
    private val questLoader: QuestLoader,
    private val questRepository: QuestRepository,
    private val canBattlefieldTileBeOccupied: CanBattlefieldTileBeOccupied,
) {
    operator fun invoke() {
        val quests = questLoader.loadQuests()
        if (quests.count { quest -> quest.type == QuestTypeDto.MAIN } != 1) throw MainQuestNotUnique()
        quests.forEach { questDto ->
            if (questRepository.searchById(questDto.id) != null) return@forEach
            val tiles = questDto.tasks.mapNotNull(TaskDto::reachTiles).flatMap(TaskDto.ReachTilesDto::tiles)
            if (tiles.any { tile -> !canBattlefieldTileBeOccupied(tile.row, tile.column) }) throw QuestTileCanNotBeOccupied()
            questRepository.save(Quest.create(questDto))
        }
    }
}
