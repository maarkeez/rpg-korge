package com.mkz.tessavar.storybook.adapters.presentation

import com.mkz.tessavar.screen.adapters.presentation.Messages
import com.mkz.tessavar.screen.adapters.presentation.QuestInfo
import com.mkz.tessavar.screen.adapters.presentation.QuestInfo.Kind
import com.mkz.tessavar.screen.adapters.presentation.QuestInfo.RewardItem
import com.mkz.tessavar.screen.adapters.presentation.QuestInfo.Task

private const val GOAT_WIZARD_QUEST = "defeat-the-goat-wizard"
private const val GOAT_WIZARD_PORTRAIT = "unit/goat-wizard_portrait.png"
private const val RAT_PORTRAIT = "unit/rat_portrait.png"
private const val ELDER_PORTRAIT = "npc/elder_portrait.png"

private fun goatWizard(messages: Messages) =
    QuestInfo(
        id = GOAT_WIZARD_QUEST,
        name = messages.questName(GOAT_WIZARD_QUEST),
        description = messages.questDescription(GOAT_WIZARD_QUEST),
        tasks = listOf(goatWizardTask(messages)),
        gold = 150,
        isMain = true,
        isPinned = true,
    )

private fun goatWizardTask(messages: Messages) = Task(Kind.DEFEAT, messages.questTask(GOAT_WIZARD_QUEST, 0), portraitPath = GOAT_WIZARD_PORTRAIT)

private fun completed(quest: QuestInfo) = quest.copy(isCompleted = true, tasks = quest.tasks.map { task -> task.copy(isDone = true, defeatsCounted = task.requiredDefeats) })

private val rats =
    QuestInfo(
        "rats",
        "Rats in the Fields",
        "Rats keep coming back to the southern fields.",
        listOf(Task(Kind.DEFEAT, "Defeat 5 Rats", defeatsCounted = 3, requiredDefeats = 5, portraitPath = RAT_PORTRAIT)),
        gold = 40,
    )
private val bees =
    QuestInfo(
        "bees",
        "Honey for the Alchemist",
        "The bees guard the honey the alchemist needs. Bring some, whenever you can.",
        listOf(Task(Kind.DEFEAT, "Defeat 3 Bees", defeatsCounted = 1, requiredDefeats = 3, portraitPath = "unit/bee_portrait.png")),
        gold = 20,
        isRepeatable = true,
    )
private val errand =
    QuestInfo(
        "errand",
        "The Elder's Errand",
        "The Elder wants to know whether the path to the southern fields is still open.",
        listOf(Task(Kind.VISIT, "Visit the southern fields", defeatsCounted = 1, isDone = true), Task(Kind.TALK, "Talk to the Elder", portraitPath = ELDER_PORTRAIT)),
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

private fun journal(messages: Messages) =
    listOf(
        goatWizard(messages),
        rats,
        bees,
        errand,
        QuestInfo(
            "bats",
            "Bats in the Meadow",
            "Bats nest in the meadow near the farm.",
            listOf(Task(Kind.DEFEAT, "Defeat 4 Bats", requiredDefeats = 4, portraitPath = "unit/bat_portrait.png")),
            gold = 35,
        ),
        QuestInfo("road", "A Road Less Travelled", "Someone must see whether the old road still leads anywhere.", listOf(Task(Kind.VISIT, "Visit the old bridge")), gold = 15),
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

enum class QuestSample(
    val title: String,
    val state: String,
    val quest: (Messages) -> QuestInfo,
) {
    GOAT_WIZARD("Goat wizard", "The quest the game ships: one task, gold only.", ::goatWizard),
    DEFEATS_COUNTED(
        "Defeats counted",
        "A defeat task under way: 3 of its 5 required defeats.",
        { messages -> goatWizard(messages).copy(tasks = listOf(Task(Kind.DEFEAT, "Defeat 5 Rats", defeatsCounted = 3, requiredDefeats = 5, portraitPath = RAT_PORTRAIT))) },
    ),
    TALK(
        "Talk",
        "A task to talk to an npc.",
        { messages -> goatWizard(messages).copy(tasks = listOf(Task(Kind.TALK, "Talk to the Elder", portraitPath = ELDER_PORTRAIT))) },
    ),
    VISIT(
        "Visit",
        "A visit task: it has no portrait.",
        { messages -> goatWizard(messages).copy(tasks = listOf(Task(Kind.VISIT, "Visit the lair down south"))) },
    ),
    SEVERAL_TASKS(
        "Several tasks",
        "Two tasks done and the current one: those still to come are not revealed.",
        { messages ->
            goatWizard(messages).copy(
                tasks =
                    listOf(
                        Task(Kind.TALK, "Talk to the Elder", defeatsCounted = 1, portraitPath = ELDER_PORTRAIT, isDone = true),
                        Task(Kind.VISIT, "Visit the lair down south", defeatsCounted = 1, isDone = true),
                        goatWizardTask(messages),
                    ),
            )
        },
    ),
    ITEM_REWARDS(
        "Item rewards",
        "A reward of gold and items.",
        { messages ->
            goatWizard(messages).copy(
                items = listOf("rune-hammer", "health-elixir", "bee-essence").map { lootableItemId -> RewardItem(lootableItemId, messages.lootableItemName(lootableItemId)) },
            )
        },
    ),
    LARGE_REWARD(
        "Large reward",
        "A reward of gold and four items, the most a row has to hold.",
        { messages ->
            goatWizard(messages).copy(
                gold = 250,
                items = listOf("rune-hammer", "health-elixir", "bee-essence", "health-potion").map { lootableItemId -> RewardItem(lootableItemId, messages.lootableItemName(lootableItemId)) },
            )
        },
    ),
    SIDE_QUEST("Side quest", "An active side quest, with a count and a task to abandon.", { rats }),
    REPEATABLE("Repeatable", "An active side quest that can run again, with its pill.", { bees }),
    PINNED("Pinned", "A side quest the player pinned, so the tracker names it.", { rats.copy(isPinned = true) }),
    PINNED_WITH_LONG_TEXT(
        "Pinned with long text",
        "The longest task, with the pin, as a translation may be.",
        {
            rats.copy(
                isPinned = true,
                tasks = listOf(Task(Kind.DEFEAT, "Derrota a las ratas de los campos del sur de una vez", defeatsCounted = 12, requiredDefeats = 50, portraitPath = RAT_PORTRAIT)),
            )
        },
    ),
    COMPLETED_REPEATABLE("Completed, repeatable", "A completed side quest, whose quest giver can start it again.", { fields }),
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
        { messages ->
            goatWizard(messages).let { quest -> quest.copy(isCompleted = true, tasks = quest.tasks.map { task -> task.copy(isDone = true, defeatsCounted = task.requiredDefeats) }) }
        },
    ),
    ;

    companion object {
        private const val LONG_NAME_PREFIX = "El encargo más largo de las montañas del sur:"
        private const val LONG_TASK_PREFIX = "Sin descanso hasta el final del camino:"

        fun journal(
            count: Int,
            isLong: Boolean,
            messages: Messages,
        ): List<QuestInfo> =
            journal(messages).take(count).map { quest ->
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
