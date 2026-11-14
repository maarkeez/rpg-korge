package com.mkz.tessavar.quest.domain

sealed class QuestError(
    message: String,
) : Throwable(message = message) {
    class QuestWithoutTasks : QuestError("A quest needs at least one task")

    class EmptyQuestReward : QuestError("A quest needs a reward of some gold or a lootable item")

    class NegativeQuestRewardGold : QuestError("The gold of a quest reward is negative")

    class InvalidTask : QuestError("A task needs the details of its type")

    class EmptyVisitTaskTiles : QuestError("A visit task needs at least one tile")

    class RequiredDefeatsBelowOne : QuestError("A defeat task needs at least 1 required defeat")

    class SideQuestWithoutQuestGiver : QuestError("A side quest needs a quest giver")

    class MainQuestWithQuestGiver : QuestError("The main quest starts on its start tiles, so it has no quest giver")

    class MainQuestWithoutStartTiles : QuestError("The main quest needs at least one start tile")

    class SideQuestWithStartTiles : QuestError("A side quest starts by talking to its quest giver, so it has no start tiles")

    class MainQuestRepeatable : QuestError("The main quest runs once, so it is not repeatable")

    class MainQuestNotUnique : QuestError("The game needs exactly one main quest")

    class MainQuestCanNotBeAbandoned : QuestError("The main quest can not be abandoned")

    class QuestNotActive : QuestError("Only an active quest can be abandoned")

    class QuestNotFound : QuestError("The quest was not found")

    class QuestTileNotVacant : QuestError("A tile of a visit task is not vacant")
}
