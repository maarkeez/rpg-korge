---
date: 3rd November 2026
commit: df10b0d826dc1cf9001b3dbd611e4a7f21e74c58
---

# 019 — Quests: Rules, Then One, Then Many

## Hook

The hero walks down a map of 40 rows. At row 36, the goat wizard waits. Defeat it, and it drops a bee essence.

Nothing in the game asks for it. Nothing even keeps count.

> Why would anyone walk down there?

Exploring had no goal. It needed quests.

**A quest needs three things, in order: rules that work, one quest the player can see, and then enough of them to choose
from.**

## Context

[The open world](014-open-world.md) gave the party a map and npcs. [The economy](015-game-economy.md) gave gold a use.

I built quests in slices, as I did the economy: three plans, 13 milestones. The scope stayed tight:

- **Three kinds of task: defeat, talk, reach.** No collecting: an item can be sold or discarded once picked up.
- **No accepting.** A side quest starts as the talk with its giver ends. Abandon undoes it.
- **No ending.** No quest chains, and no failing either.

## Main Story — Three Slices

### 1. A quest nobody could see

The first slice had no view at all. A quest is a line of tasks and a reward, in one TOML file:

```toml
type = "MAIN"

[[tasks]]
type = "DEFEAT_UNITS"
unitId = "goat-wizard"
count = 1

[reward]
gold = 150
```

**When does a rat count?** Only once its task is the current one. Counting from the start of the game needs totals of
every possible task.

**Who gets paid?** Whoever leads when the last task completes. What doesn't fit the bag drops on the leader's tile, like
any [loot](012-add-loot-mechanic.md): no pending claim, nothing lost.

**One aggregate or two?** I considered a `Quest` and a `QuestProgress`, mirroring `Unit` and `BattleUnit`. But that split
exists because a unit has many battle units. A quest has one running copy.

Then it ran. The goat wizard fell, and the hero went from 30 gold to 180.

Without a word. You had to open the inventory to notice.

### 2. One quest on the screen

The second slice only drew the main quest. I prototyped three variants of each piece in
[the Storybook](017-storybook.md), then rebuilt the chosen ones:

- **The tracker:** a capsule on the top line: `Defeat the Goat Wizard`.
- **The detail view:** tapping it opens the quest, with time stopped.
- **The announcement:** a card over the dimmed screen, until Continue.

The card felt right. It happens once per game, and it tells the goal without a tap.

### 3. Many quests

The third slice added a journal, a sign over the head of who to talk to, Abandon in red, and three real side quests:

```text
Bats in the Meadow   Farmer   defeat 3 bats, talk       40 G
The Elder's Errand   Elder    reach the fields, talk    25 G + health potion
Rats in the Fields   Elf      defeat 2 rats             20 G, repeatable
```

They shipped in the last milestone. Until then, only the tests played them.

#### The card had to go

Rats in the Fields can run forever. Every start would dim the screen, stop time and wait for Continue.

A moment worth stopping for once per game is an interruption on the fifth rat hunt.

So one slice after building it, I removed it. Every quest, the main one too, now gets a toast over the battlefield: held
4 seconds, fading in 0.5, taking no taps and holding nothing.

**Nothing was wrong with the card. It had only ever met one quest.**

#### Which quest does the tracker name?

One capsule, many quests.

> Which one should it name?

A badge counting the others never shows a side quest's task. The quest that progressed last changes by itself, mid-fight.

So the player decides. Pin a quest in the journal, and the tracker names it. The main quest is the default. A quest
starting doesn't take the pin, or a repeatable one would keep grabbing the tracker.

### What is still unfinished

- **Givers say nothing.** The Farmer only ever says "Thank you for dealing with that bat!", before, during and after its
  quest. A missed toast never comes back.
- **No ending.** You defeat the goat wizard, collect 150 gold, and keep exploring.

## Key Takeaways

- **Rules before screens.** The system shipped with no view, so every screen after it only displayed what already worked.
- **One case is not the design.** A decision made for one quest gets tested by the second kind.
- **Prefer who decides.** A pin stays where the player put it.
- **Count from now.** A task counting only from when it is current needs no history of the game.
- **Ship content last.** With the real side quests in the final milestone, nobody meets a half-finished screen.

## What's Next

The quests work. They don't tell a story yet.

Start a new game, and a toast hands you the main quest. Nobody asks. Nobody explains why a goat rules a lair down south.

Two things come next. A better dialog system, where each npc says something different depending on the status of a
quest. And a way to deliver the main quest through a narrative, instead of an announcement.

A system can tell the player what to do. Only a story tells them why.
