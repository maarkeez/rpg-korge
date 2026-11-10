---
date: 10th November 2026
commit: 8692ff6f0fe556e6e9142583d589630779a6e17e
---

# 021 — Speaking the Same Language

## Hook

Open a party member's inventory. The title says Inventory.

Walk into the shop. It says Bag 11/12.

Tap an item. Its actions say Bags full.

> Is the bag the inventory?

Yes. Three words, one thing. And the code had its own: the most gold an inventory can carry was a "cap" in one comment,
a "limit" in `GoldAboveLimit`, and "the most it can carry" in another.

I knew they were the same. A new contributor wouldn't. Neither would a coding agent, which writes much of this game and
learns its words from the code it reads.

**A ubiquitous language only works when every contributor shares it, humans and AI agents alike.**

## Context

In [the first entry](001-how-i-started-my-game.md), a ubiquitous language document was one of the first things I made.
But the repository never had one. The words lived in the code, and in my head.

[The architecture redesign](016-architecture-redesign.md) changed what was possible. Every subdomain became one package:
`battle`, `battleUnit`, `inventory`, `quest`, 25 of them. For the first time, the language could be settled
one package at a time.

The goal: one word per concept, everywhere. In the code, the TOML data, the saves, and what players read, in English
and Spanish.

The scope, on purpose:

- **One section per package.**
- **Only the game's words.** No general programming concepts.
- **Words first, code after.** A review only settles words and writes a plan of renames. The renames are built after.

## Ubiquitous Language, in Short

Domain-driven design asks the people who build software and the people who know the problem to speak one language. Eric
Evans calls it a ubiquitous language: the words of the conversations, the documents and the code are the same words,
with the same meaning.

A subdomain is one part of the problem: the battle, the inventory, the quests. Each has its own words, and a word can
mean something else in another one.

In a game made by one person, those people are me and a coding agent. And the agent reads far more of the code than I
do.

## Main Story — One Subdomain at a Time

### Why not the whole glossary at once?

Words lean on each other. Walk needs Path and Tile. Restore needs Mana.

Twenty-five packages in one pass meant deciding hundreds of words before seeing any of them in the code. So I went
alphabetically, from `ability` to `unit`, and each section was renamed in the code before the next one started. Every
review read code that already spoke the earlier sections' words.

A coding agent interviewed me about each package, with scenarios using the real units: a rat two tiles from the knight,
a bat summoned for 2 player turns. Each answer became an entry:

```text
**Inventory**:
The stored items, equipped items and gold of a party member.
_Avoid_: Bag
```

**A definition says what a thing is, not how it works.** Cooldown is "the wait an ability imposes after a cast before it
can be cast again", with no word about how the turns count it down.

The word that lost goes under _Avoid_, and into the plan of renames.

### Player's word or code's word?

No rule. Decided term by term.

Bag became Inventory, for players too. But players still read HP, MP and KO, and a bare "turn", as they read them well.

### Earlier words moved

`building` decided that a building is placed, not initialised. So `npc` had to place its npcs too, and `quest` had to
find its own word: it creates its quests.

`effect` gave Restore to a battle unit getting mana back. So a save is now loaded, not restored, and `GameRestored`
became `GameLoaded`.

`party` paused the exploration when a battle started. But pausing is what the player does from the pause screen. So a
battle now interrupts the exploration.

```text
Restore     a battle unit getting mana back
Load        taking a save back into play
Pause       the pause screen
Interrupt   a battle stopping the party exploring
```

### The price

**Renaming a saved field breaks every save written before it.**

The game has no save migrations, on purpose. So each rename that reached the save format bumped `SAVE_VERSION`, from 4
to 9. Every older save is now an incompatible save: it can only be deleted or overwritten.

In numbers: 25 sections, 321 terms, 155 words to avoid, 25 plans and 80 commits. About a thousand files changed under
`src` and `resources`.

And the game plays the same.

## Key Takeaways

- **One word, one concept.** When a word means two things, someone will read the wrong one, human or agent.
- **Write the words down.** A language in your head can't be shared. Keep each definition to what a thing is.
- **Go subdomain by subdomain.** Small decisions, each one built before the next.
- **Rename everything.** A glossary the code ignores is a wish.
- **Let words move.** A later section can prove an earlier word wrong. Rename it again.

## What's Next

With the words settled, the architecture docs were next. They are now 13 ADRs, recording only the decisions that are
hard to reverse.

The bigger change is how a feature starts. Next, every feature starts in the glossary: the words it needs, and the ones
it can't reuse.

A shared language isn't written once. It's kept.
