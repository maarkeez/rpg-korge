package com.mkz.rpg.storybook.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.QuestInfo
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.RewardItem
import com.mkz.rpg.screen.adapters.presentation.QuestInfo.Task
import com.mkz.rpg.screen.adapters.presentation.Texts

private const val GOAT_WIZARD_QUEST = "defeat-the-goat-wizard"
private const val GOAT_WIZARD_PORTRAIT = "unit/goat-wizard_portrait.png"
private const val RAT_PORTRAIT = "unit/rat_portrait.png"
private const val ELDER_PORTRAIT = "npc/elder_portrait.png"

// The quest the game ships, with the texts of the language global, which the other samples are built on.
private fun goatWizard(texts: Texts) =
    QuestInfo(
        id = GOAT_WIZARD_QUEST,
        name = texts.questName(GOAT_WIZARD_QUEST),
        description = texts.questDescription(GOAT_WIZARD_QUEST),
        tasks = listOf(goatWizardTask(texts)),
        gold = 150,
        isMain = true,
    )

private fun goatWizardTask(texts: Texts) = Task(Kind.DEFEAT, texts.questTask(GOAT_WIZARD_QUEST, 0), portraitPath = GOAT_WIZARD_PORTRAIT)

private fun completed(quest: QuestInfo) = quest.copy(isCompleted = true, tasks = quest.tasks.map { task -> task.copy(isDone = true, progress = task.count) })

private val rats =
    QuestInfo(
        "rats",
        "Rats in the Fields",
        "Rats keep coming back to the southern fields.",
        listOf(Task(Kind.DEFEAT, "Defeat 5 Rats", progress = 3, count = 5, portraitPath = RAT_PORTRAIT)),
        gold = 40,
    )
private val bees =
    QuestInfo(
        "bees",
        "Honey for the Alchemist",
        "The bees guard the honey the alchemist needs. Bring some, whenever you can.",
        listOf(Task(Kind.DEFEAT, "Defeat 3 Bees", progress = 1, count = 3, portraitPath = "unit/bee_portrait.png")),
        gold = 20,
        isRepeatable = true,
    )
private val errand =
    QuestInfo(
        "errand",
        "The Elder's Errand",
        "The Elder wants to know whether the path to the southern fields is still open.",
        listOf(Task(Kind.REACH, "Reach the southern fields", progress = 1, isDone = true), Task(Kind.TALK, "Talk to the Elder", portraitPath = ELDER_PORTRAIT)),
        gold = 25,
    )
private val fields =
    completed(
        QuestInfo(
            "fields",
            "The Farmer's Fields",
            "The Farmer's fields are overrun again and again.",
            listOf(Task(Kind.TALK, "Talk to the Farmer", portraitPath = "npc/farmer_portrait.png")),
            gold = 30,
            isRepeatable = true,
        ),
    )

// A journal of 12, in the order it lists them: the main quest, the active side quests, and the completed ones.
private fun journal(texts: Texts) =
    listOf(
        goatWizard(texts),
        rats,
        bees,
        errand,
        QuestInfo("bats", "Bats in the Meadow", "Bats nest in the meadow near the farm.", listOf(Task(Kind.DEFEAT, "Defeat 4 Bats", count = 4, portraitPath = "unit/bat_portrait.png")), gold = 35),
        QuestInfo("road", "A Road Less Travelled", "Someone must see whether the old road still leads anywhere.", listOf(Task(Kind.REACH, "Reach the old bridge")), gold = 15),
        QuestInfo(
            "princess",
            "A Word with the Princess",
            "The Princess asks for whoever defeated the rats.",
            listOf(Task(Kind.TALK, "Talk to the Princess", portraitPath = "npc/princess_portrait.png")),
            gold = 50,
        ),
        QuestInfo(
            "viking",
            "The Viking's Challenge",
            "A viking will not leave until someone beats it.",
            listOf(Task(Kind.DEFEAT, "Defeat the Viking Warrior", portraitPath = "unit/viking-warrior_portrait.png")),
            gold = 80,
        ),
        QuestInfo(
            "shelves",
            "The Alchemist's Shelves",
            "The Alchemist needs a hand with some heavy shelves.",
            listOf(Task(Kind.TALK, "Talk to the Alchemist", portraitPath = "npc/alchemist_portrait.png")),
            gold = 12,
        ),
        QuestInfo("elf", "The Elf's Errand", "The Elf has a message that can not wait.", listOf(Task(Kind.TALK, "Talk to the Elf", portraitPath = "npc/elf_portrait.png")), gold = 18),
        fields,
        completed(
            QuestInfo("riddle", "The Elf's Riddle", "The Elf offers a riddle to anyone who listens.", listOf(Task(Kind.TALK, "Talk to the Elf", portraitPath = "npc/elf_portrait.png")), gold = 10),
        ),
    )

/** The quests the stories display: the one the game ships, and the others the backend allows. */
enum class QuestSample(
    val title: String,
    val state: String,
    val quest: (Texts) -> QuestInfo,
) {
    GOAT_WIZARD("Goat wizard", "The quest the game ships: one task, gold only.", ::goatWizard),
    COUNTED(
        "Counted",
        "A task with a count, under way: 3 of 5.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.DEFEAT, "Defeat 5 Rats", progress = 3, count = 5, portraitPath = RAT_PORTRAIT))) },
    ),
    TALK(
        "Talk",
        "A task to talk to an npc.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.TALK, "Talk to the Elder", portraitPath = ELDER_PORTRAIT))) },
    ),
    REACH(
        "Reach",
        "A task to reach some tiles: it has no portrait.",
        { texts -> goatWizard(texts).copy(tasks = listOf(Task(Kind.REACH, "Reach the lair down south"))) },
    ),
    SEVERAL_TASKS(
        "Several tasks",
        "Two tasks done and the current one: those still to come are not revealed.",
        { texts ->
            goatWizard(texts).copy(
                tasks =
                    listOf(
                        Task(Kind.TALK, "Talk to the Elder", progress = 1, portraitPath = ELDER_PORTRAIT, isDone = true),
                        Task(Kind.REACH, "Reach the lair down south", progress = 1, isDone = true),
                        goatWizardTask(texts),
                    ),
            )
        },
    ),
    ITEM_REWARDS(
        "Item rewards",
        "A reward of gold and items.",
        { texts ->
            goatWizard(texts).copy(
                items = listOf("rune-hammer", "health-elixir", "bee-essence").map { lootableItemId -> RewardItem(lootableItemId, texts.lootableItemName(lootableItemId)) },
            )
        },
    ),
    LARGE_REWARD(
        "Large reward",
        "A reward of gold and four items, the most a row has to hold.",
        { texts ->
            goatWizard(texts).copy(
                gold = 250,
                items = listOf("rune-hammer", "health-elixir", "bee-essence", "health-potion").map { lootableItemId -> RewardItem(lootableItemId, texts.lootableItemName(lootableItemId)) },
            )
        },
    ),
    SIDE_QUEST("Side quest", "An active side quest, with a count and a task to abandon.", { rats }),
    REPEATABLE("Repeatable", "An active side quest that can run again, with its pill.", { bees }),
    COMPLETED_REPEATABLE("Completed, repeatable", "A completed side quest, whose giver can start it again.", { fields }),
    LONG_TEXT(
        "Long text",
        "The longest texts, as a translation may be: a long name, description and task, and the largest gold.",
        {
            QuestInfo(
                id = GOAT_WIZARD_QUEST,
                name = "La guarida del mago cabra en las montañas del sur",
                description = "Hace mucho tiempo una cabra se comió el libro de hechizos de un mago y se tragó toda su magia. Ahora gobierna la guarida del sur, y nadie se atreve a acercarse.",
                tasks = listOf(Task(Kind.DEFEAT, "Derrota al mago cabra de la guarida del sur", portraitPath = GOAT_WIZARD_PORTRAIT)),
                gold = 99_999,
                isMain = true,
            )
        },
    ),
    COMPLETED(
        "Completed",
        "The goat wizard defeated and its reward paid.",
        { texts ->
            goatWizard(texts).let { quest -> quest.copy(isCompleted = true, tasks = quest.tasks.map { task -> task.copy(isDone = true, progress = task.count) }) }
        },
    ),
    ;

    companion object {
        private const val LONG_NAME_PREFIX = "El encargo más largo de las montañas del sur:"
        private const val LONG_TASK_PREFIX = "Sin descanso hasta el final del camino:"

        /** The quests of a journal listing [count] of them, as long as a translation may be when [isLong]. */
        fun journal(
            count: Int,
            isLong: Boolean,
            texts: Texts,
        ): List<QuestInfo> =
            journal(texts).take(count).map { quest ->
                if (isLong) {
                    quest.copy(
                        name = "$LONG_NAME_PREFIX ${quest.name}",
                        tasks = quest.tasks.map { task -> task.copy(text = "$LONG_TASK_PREFIX ${task.text}") },
                    )
                } else {
                    quest
                }
            }
    }
}
