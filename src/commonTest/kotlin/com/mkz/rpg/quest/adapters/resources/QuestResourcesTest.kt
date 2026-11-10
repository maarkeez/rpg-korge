package com.mkz.rpg.quest.adapters.resources

import com.mkz.rpg.lootableItem.adapters.resources.ResourcesLootableItemLoader
import com.mkz.rpg.npc.adapters.resources.ResourcesNpcLoader
import com.mkz.rpg.quest.domain.Quest
import com.mkz.rpg.quest.domain.Quest.Dto.QuestTypeDto
import com.mkz.rpg.quest.domain.Quest.Dto.TaskDto
import com.mkz.rpg.unit.adapters.resources.ResourcesUnitLoader
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class QuestResourcesTest {
    private suspend fun quests(): List<Quest.Dto> = ResourcesQuestLoader().also { loader -> loader.initResources() }.loadQuests()

    @Test
    fun `should find the unit of every defeat task in the unit resources`() =
        runTest {
            // Given
            val defeatTasks = quests().flatMap(Quest.Dto::tasks).mapNotNull(TaskDto::defeat)
            // When
            val unitIds =
                ResourcesUnitLoader()
                    .also { loader -> loader.initResources() }
                    .loadUnits()
                    .map { unit -> unit.id }
                    .toSet()
            // Then
            defeatTasks.shouldNotBeEmpty()
            defeatTasks.map(TaskDto.DefeatDto::unitId).filterNot(unitIds::contains).shouldBeEmpty()
        }

    @Test
    fun `should find the npc of every talk task and quest giver, and none recruitable, in the npc resources`() =
        runTest {
            // Given
            val quests = quests()
            val npcIds = quests.flatMap(Quest.Dto::tasks).mapNotNull(TaskDto::talk).map(TaskDto.TalkDto::npcId) + quests.mapNotNull(Quest.Dto::questGiverNpcId)
            // When
            val npcs = ResourcesNpcLoader().also { loader -> loader.initResources() }.loadNpcs()
            val notRecruitableNpcIds = npcs.filter { npc -> npc.unitId == null }.map { npc -> npc.id }.toSet()
            // Then
            npcIds.filterNot(notRecruitableNpcIds::contains).shouldBeEmpty()
        }

    @Test
    fun `should find every lootable item of a reward in the lootable item resources`() =
        runTest {
            // Given
            val rewardItemIds = quests().flatMap { quest -> quest.reward.lootableItemIds }
            // When
            val lootableItemIds =
                ResourcesLootableItemLoader()
                    .also { loader -> loader.initResources() }
                    .loadLootableItems()
                    .map { item -> item.id }
                    .toSet()
            // Then
            rewardItemIds.filterNot(lootableItemIds::contains).shouldBeEmpty()
        }

    @Test
    fun `should hold exactly one main quest`() =
        runTest {
            // When
            val mainQuests = quests().filter { quest -> quest.type == QuestTypeDto.MAIN }
            // Then
            mainQuests.shouldHaveSize(1)
        }
}
