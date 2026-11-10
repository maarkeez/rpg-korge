# Plan: an npc says the line that fits its quest

> **Status:** to do.
>
> **Summary:** Today every npc has a single line, `npc.<id>.line`, whatever its quests say. The Farmer thanks the party
> for the gate bat before, during and after Bats in the Meadow, and never asks for the 3 bats. The Elder tells the Goat
> Wizard's story even once the Goat Wizard is defeated. A quest is announced when the talk finishes with nobody having
> asked for it. Instead, a quest has quest lines for its npcs. Its quest giver says a start line in the talk that starts
> it, a reminder line while it is active, and a completed line once it is done, or a start again line when it is
> repeatable. The npc of a talk task says a talk task line in the talk that does it. Any other time an npc says its own
> line. A talk reads one line and does only what that line says. It builds on the glossary's **Line**, **Own line** and
> **Quest line**, on [a quest owns the lines its npcs say about it](../adr/0016-a-quest-owns-the-lines-its-npcs-say-about-it.md)
> and on [the main quest starting at the village entrance](main-quest-start-tiles.md).

## Decisions

| Topic              | Decision                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope              | The lines of the 3 side quests and of the main quest's talk task, which line a talk reads, and what a talk does. E.g.: the Farmer asks for the 3 bats, reminds the party of them, pays for them and then says the meadow is quiet.                                                                                                                                                                                                                     |
| One line per talk  | A talk reads one line, and a tap finishes it, as today. Chosen over several lines tapped through in one talk, which changes the talk panel and what a talk is.                                                                                                                                                                                                                                                                                         |
| Quest giver        | A quest giver has a start line while its side quest is not started, a reminder line while it is active, and a completed line once it is completed. Chosen over one line per status, as the Farmer would ask "Any luck with the bats?" in the very talk that completes the quest, and over one line per current task, which is the most to write.                                                                                                       |
| Talk task          | The npc of a talk task, quest giver or not, says a talk task line in the talk that does that task. E.g.: the Elder, who gives no quest, tells the Goat Wizard's story only while "Talk to the Elder" is the current task, and its own line before and after. Chosen over lines for any npc at any moment of any quest, which has to be declared line by line.                                                                                          |
| Repeatable         | A completed repeatable quest has a start again line in place of the completed line, as the next talk starts it again and no completed line could ever be read. E.g.: the Elf, once Rats in the Fields is completed. Chosen over repeating the start line, where the Elf never notices the party's work, and over a talk that only thanks before the one that starts it again, which changes when the quest marker shows.                               |
| Abandon            | An abandoned quest is not started, so its quest giver says the start line again, also for a repeatable quest completed before. Chosen over remembering the completion, which adds a field to the save.                                                                                                                                                                                                                                                 |
| Required           | Every quest line is required, and nothing declares them in the toml: they follow from the quest giver, the talk tasks and `repeatable`. A reminder line is required only when the quest has a task that is not a talk with its quest giver, as it could not be read otherwise. Chosen over optional lines falling back to the own line, which each quest would have to declare.                                                                        |
| Own line           | A quest giver has no own line, as its side quest has a line for it whatever its status. The Farmer, the Elf and the Blacksmith lose `npc.<id>.line`, and the other 5 npcs keep theirs.                                                                                                                                                                                                                                                                 |
| One thing per talk | A talk does one talk task or starts one quest: the one whose line is read. A reminder line and a completed line do nothing. E.g.: if the Elder gave a side quest, one talk does "Talk to the Elder" and the next one reads the start line and starts the side quest. Chosen over doing everything in one talk, as today, where a quest starts with its start line unread, and over forbidding an npc from being in two quests. No data needs it today. |
| Order              | When several quests have a line for the npc: a talk task line, then a start or start again line, then a reminder line, then a completed line, as the quest markers already prefer a talk task. Among lines of the same kind, the main quest goes first, then the quests by their id. Chosen over an order number in each toml.                                                                                                                         |
| Starting           | A side quest still starts by itself when the talk finishes. Accepting and declining are out of scope.                                                                                                                                                                                                                                                                                                                                                  |
| Merchant           | The Blacksmith stays a merchant and a quest giver: its quest line is read over Leave, Sell and Buy, the same line is back when the shop is closed, and its errand starts on Leave.                                                                                                                                                                                                                                                                     |
| Keys               | `quest.<quest id>.line.start`, `.line.reminder`, `.line.completed` and `.line.startAgain`, and `quest.<quest id>.line.task.<task index>` for a talk task line. The own line stays `npc.<npc id>.line`.                                                                                                                                                                                                                                                 |
| Writing            | The 12 new lines and the Elder's new own line are written in this plan, in English and Spanish. See [Catalogs](#catalogs).                                                                                                                                                                                                                                                                                                                             |
| Architecture       | `quest.domain.Quest` knows the line it has for an npc. `quest.usecases.queries.SearchQuestLine` picks one among the quests, for `quest.usecases.commands.ResolveNpcTalk` and for `screen.adapters.presentation.ExplorationPresenter`, which falls back to the own line. `npc` and `quest` stay independent. Chosen over the npc listing all its lines, and over the screen picking the line, which puts a game rule in the screen.                     |
| Picked twice       | The screen asks for the line when the talk starts, and `ResolveNpcTalk` asks again when it finishes. They agree, as time is stopped during a talk and neither the journal nor a battle can change a quest meanwhile. Chosen over carrying the line in `NpcTalkFinished`, which `party` publishes without knowing the quests.                                                                                                                           |
| Saves              | Nothing is saved: the line follows from the progress of the quests. `Save.SAVE_VERSION` stays 9, and older saves load.                                                                                                                                                                                                                                                                                                                                 |
| Storybook          | No story changes: the talk panel draws a line as before, and `TalkViewStories` keeps its own texts.                                                                                                                                                                                                                                                                                                                                                    |
| Tests              | Unit tests for `Quest`, `SearchQuestLine`, `ResolveNpcTalk`, `QuestMapper`, `Messages` and `ExplorationPresenter`. `LocalizationResourcesTest` requires every quest line and own line. `QuestAcceptanceTest` and `QuestEndToEndTest` talk to the Farmer through Bats in the Meadow.                                                                                                                                                                    |
| Out of scope       | Several lines in one talk, accepting or declining a quest, counting how often a quest was completed, npcs outside a quest reacting to it (the Princess, or the Elder once the Goat Wizard is defeated), and a recruitable quest giver.                                                                                                                                                                                                                 |

For example, the talks with the Farmer through Bats in the Meadow, "Defeat 3 Bats" and then "Talk to the Farmer":

| Talk | Bats in the Meadow before the talk | Line read      | When the talk finishes                         |
|------|------------------------------------|----------------|------------------------------------------------|
| 1st  | Not started                        | Start line     | The quest starts, and "NEW QUEST" is announced |
| 2nd  | Active, on "Defeat 3 Bats", 1/3    | Reminder line  | Nothing                                        |
| 3rd  | Active, on "Talk to the Farmer"    | Talk task line | The quest is completed, and grants 40 gold     |
| 4th  | Completed                          | Completed line | Nothing                                        |

And the talks with the Elf through Rats in the Fields, "Defeat 2 Rats", which is repeatable:

| Talk | Rats in the Fields before the talk | Line read        | When the talk finishes |
|------|------------------------------------|------------------|------------------------|
| 1st  | Not started                        | Start line       | The quest starts       |
| 2nd  | Active, on "Defeat 2 Rats", 0/2    | Reminder line    | Nothing                |
| 3rd  | Completed, on its 2nd rat          | Start again line | The quest starts again |
| 4th  | Active, then abandoned             | Start line       | The quest starts       |

## quest

### Domain

- `Quest.Dto.QuestLineDto(questId, kind, taskIndex)` is the line a quest has for an npc. Its `KindDto` is `START`,
  `REMINDER`, `TALK_TASK`, `COMPLETED` or `START_AGAIN`, and `taskIndex` is the place of the talk task for a
  `TALK_TASK`, and null otherwise.
- `Quest.lineFor(npcId)` returns the line the quest has for the npc now, or null:
  - `TALK_TASK`, with the current task's index, when the quest is active and its current task is a talk with the npc,
    whether the npc is its quest giver or not.
  - Otherwise null when the npc is not its quest giver. E.g.: the main quest, which has none, only ever has talk task
    lines.
  - `START` when it is not started, `REMINDER` when it is active, `START_AGAIN` when it is completed and repeatable,
    and `COMPLETED` when it is completed and not repeatable.
- `Quest.canBeStartedBy`, `markerFor`, `start` and `talkedTo` do not change.

### Use cases

- `SearchQuestLine(npcId)`, a new query, returns the one `QuestLineDto` the npc says now, or null when no quest has a
  line for it. It orders the quests with the main quest first and then by id, takes `lineFor(npcId)` of each, and
  returns the first of the best kind: `TALK_TASK`, then `START` or `START_AGAIN`, then `REMINDER`, then `COMPLETED`.
  E.g.: for an Elder who also gives a side quest, it returns the main quest's talk task line, and the side quest's start
  line once that task is done.
- `ResolveNpcTalk(npcId)` does only what `SearchQuestLine(npcId)` says. It publishes `talkedTo(npcId)` of that quest
  for a `TALK_TASK`, and `start()` of that quest for a `START` or a `START_AGAIN`. It does nothing for a `REMINDER`, a
  `COMPLETED` or no line. It takes `SearchQuestLine` besides `QuestRepository` and `PublishQuestProgress`. E.g.: a talk
  with a Farmer who gives two quests not started starts only the first by id, and the next talk starts the other.

### Adapters

- `QuestApi` builds `searchQuestLine`, exposes it, and passes it to `ResolveNpcTalk`.
- `OnNpcTalkFinished` does not change.

## screen

- `Messages.questLine(questId, line)` translates `quest.<questId>.line.<line>`. E.g.:
  `questLine("bats-in-the-meadow", "task.1")`.
- `Messages.npcLine` is renamed `npcOwnLine`, after the glossary's own line.
- `QuestMapper.lineOf(line, messages)` translates a `QuestLineDto`: `start`, `reminder`, `completed` and `startAgain`
  for their kinds, and `task.<taskIndex>` for a `TALK_TASK`.
- `ExplorationPresenter` takes the `QuestApi`. Its `displayNpcDialog(npc)` is renamed `displayTalkPanel(npc)`, as the
  glossary avoids "dialog", and displays `QuestMapper.lineOf` of `questApi.searchQuestLine(npc.id)`, or else
  `messages.npcOwnLine(npc.id)`. It runs when the talk starts and when the shop of a merchant is closed, so the
  Blacksmith's line is the same on both.
- `GameScene` passes its `questApi` to the `ExplorationPresenter`.

## Catalogs

`resources/localization/messages.pot` and `es.po`. No toml changes.

- `npc.farmer.line`, `npc.elf.line` and `npc.blacksmith.line` are deleted.
- Each new entry has a comment naming its line. E.g.: "What the quest giver says in the talk that starts the quest."
- The comment of the task texts reads "by its place in the series of tasks", and the comment over the game data reads
  "and the own lines of the npcs".
- `quest.bats-in-the-meadow`, said by the Farmer:
  - `line.start`: "Thank you for dealing with that bat! More of them nest in my meadow, off the path. Defeat three of
    them and I will pay you well. My village is just south."
    - "¡Gracias por libraros de ese murciélago! Hay más anidando en mi prado, fuera del camino. Derrotad a tres y os
      pagaré bien. Mi aldea está justo al sur."
  - `line.reminder`: "The bats still nest in my meadow, off the path. Three less of them, and I can sleep again."
    - "Los murciélagos siguen anidando en mi prado, fuera del camino. Con tres menos, podré volver a dormir."
  - `line.task.1`: "Three bats gone! The meadow is quiet at last. Here, take this gold: you have earned it."
    - "¡Tres murciélagos menos! Por fin el prado está en calma. Tomad este oro: os lo habéis ganado."
  - `line.completed`: "No bat has bothered me since. My village is just south, past this meadow: tell them the Farmer
    sent you."
    - "Ningún murciélago me ha molestado desde entonces. Mi aldea está justo al sur, pasado este prado: decid que os
      envía el Granjero."
- `quest.the-blacksmiths-errand`, said by the Blacksmith:
  - `line.start`: "Fresh from the forge! Before you buy: my iron comes up through the southern fields. Go and see
    whether the path is still open, and I will make it worth your while."
    - "¡Recién salido de la forja! Antes de comprar: mi hierro llega por los campos del sur. Id a ver si el paso sigue
      abierto, y os lo sabré agradecer."
  - `line.reminder`: "Have you been to the southern fields yet? I need to know whether that path is open. Meanwhile, a
    sturdy hammer keeps you standing for longer."
    - "¿Habéis ido ya a los campos del sur? Necesito saber si ese paso sigue abierto. Mientras tanto, un buen martillo
      os mantiene en pie más tiempo."
  - `line.task.1`: "The path is open? Good news for my forge! Take this gold, and a potion for the road."
    - "¿El paso está abierto? ¡Buena noticia para mi forja! Tomad este oro, y una poción para el camino."
  - `line.completed`: "Fresh from the forge! A sturdy hammer keeps you standing for longer. And thank you again for
    walking to the fields."
    - "¡Recién salido de la forja! Un buen martillo os mantiene en pie más tiempo. Y gracias de nuevo por acercaros a
      los campos."
- `quest.rats-in-the-fields`, said by the Elf:
  - `line.start`: "The forest whispers of strangers who outwit goats and bats alike. Then rats will not trouble you:
    thin them out in the southern fields, and I pay in gold."
    - "El bosque habla de forasteros que burlan a cabras y murciélagos por igual. Entonces las ratas no os darán
      problemas: ahuyentadlas de los campos del sur, y pago en oro."
  - `line.reminder`: "Two rats, travellers, in the southern fields. My arrows stay here, watching over this village."
    - "Dos ratas, viajeros, en los campos del sur. Mis flechas se quedan aquí, vigilando esta aldea."
  - `line.startAgain`: "Well done, travellers. But the rats are back in the southern fields, as always. Thin them out
    again: same task, same gold."
    - "Bien hecho, viajeros. Pero las ratas han vuelto a los campos del sur, como siempre. Ahuyentadlas otra vez: misma
      tarea, mismo oro."
- `quest.defeat-the-goat-wizard`, said by the Elder:
  - `line.task.0`: the text and the translation `npc.elder.line` has today, "Long ago a goat chewed up a wizard's
    spellbook and swallowed its magic. The Goat Wizard now rules the lair down south. Beat it, and its bees may answer
    your call."
- `npc.elder.line`, the Elder's own line, fits before the main quest starts and after its talk task: "The lair lies
  down south. Tread carefully, travellers: a goat never forgets."
  - "La guarida queda al sur. Andad con cuidado, viajeros: una cabra nunca olvida."

## Docs

- [A quest owns the lines its npcs say about it](../adr/0016-a-quest-owns-the-lines-its-npcs-say-about-it.md) and the
  glossary's **Lines** already say this, so neither changes.

## Tests

- `QuestTest`
  - `LineFor should be the start line when the npc is the quest giver of a quest not started`
  - `LineFor should be the reminder line when the quest is active and its current task is no talk with its quest giver`
  - `LineFor should be the talk task line, with the place of its task, when the current task is to talk to the npc`
  - `LineFor should be the talk task line when the npc of the current talk task is no quest giver`
  - `LineFor should be the completed line when the quest is completed and not repeatable`
  - `LineFor should be the start again line when a repeatable quest is completed`
  - `LineFor should be the start line again when a repeatable quest completed before is abandoned`
  - `LineFor should be nothing when the npc is neither the quest giver nor the npc of the current talk task`
- `SearchQuestLineTest`
  - `SearchQuestLine should be nothing when no quest has a line for the npc`
  - `SearchQuestLine should prefer a talk task line when an npc also has a quest to start`
  - `SearchQuestLine should prefer a start line when the npc also has a reminder line`
  - `SearchQuestLine should prefer a start again line when the npc also has a reminder line`
  - `SearchQuestLine should prefer a reminder line when the npc also has a completed line`
  - `SearchQuestLine should prefer the main quest when two quests ask to talk to the npc`
  - `SearchQuestLine should prefer the first quest by id when the npc has two quests to start`
- `ResolveNpcTalkTest`
  - `ResolveNpcTalk should start only the first side quest by id when its quest giver has two to start` (replaces
    `ResolveNpcTalk should start every eligible side quest of the quest giver`)
  - `ResolveNpcTalk should start the other side quest when a later talk finishes`
  - `ResolveNpcTalk should do the talk task and start no quest when the npc has both`
  - `ResolveNpcTalk should do only the talk task of the main quest when two quests ask to talk to the npc`
  - `ResolveNpcTalk should change nothing when the quest giver says its reminder line`
  - The other 6 tests stay as they are.
- `QuestMapperTest`
  - `lineOf should translate the start, reminder, completed and start again lines of a quest`
  - `lineOf should translate the line of a talk task by the place of its task`
- `MessagesTest`
  - `questLine should return the line of the quest`
  - `selectLanguage should translate the messages and the game data into the language selected` reads
    `npcOwnLine("viking")`.
- `ExplorationPresenterTest`
  - `TalkToAdjacentNpc should display the talk panel of the npc next to the leader in place of the pad when A is
    pressed` expects the Elder's own line, as the main quest has not started.
  - `TalkToAdjacentNpc should display the talk task line when the current task is to talk to the npc`
  - `TalkToAdjacentNpc should display the start line of the quest the npc gives`
  - `ShopBackSelected should display the quest line of the merchant again when the shop is closed`
- `QuestPresenterTest`
  - `QuestPresenter should play the announcements one after the other when two quests start` (renames the one ending
    in `when a talk starts two quests`, as a talk no longer does)
- `LocalizationResourcesTest`
  - `should have every line of every quest in the source catalog`: the start, reminder and completed or start again
    lines of each side quest, and a line per talk task of every quest.
  - `should have an own line in the source catalog for every npc that gives no side quest`: `gameDataFields` only
    asks for the `name` of an npc.
  - `should only have the keys the screen translates in the source catalog` counts the quest lines and the own lines,
    so a line left behind for the Farmer, the Elf or the Blacksmith fails.
- `QuestAcceptanceTest`
  - `An npc should do its talk task in one talk and start the side quest it gives in the next`
- `QuestEndToEndTest`
  - `should say the start line, then the reminder line, when the farmer is talked to twice`
  - `should play a side quest when it is started, pinned, progressed and completed` also reads the Farmer's talk task
    line in the talk that completes the quest.

## Known limitations

- No test checks that a line fits the talk panel. The longest line today, the Elder's story in Spanish, fits, and the
  new ones are shorter.
- The Elder says the same own line before the main quest starts, while the Goat Wizard is to defeat, and once it is
  defeated. The Princess keeps her one line.
- A talk with the Elder before the main quest starts, after a teleport into the village, reads its own line and counts
  for nothing, as before.
- The test helpers of the talk panel still say "dialog": `TalkView.tapDialog`, `clickDialog` and `isDialogShown`.
