---
date: 19th October 2026
commit: e758712e4eb5887e23e8d88c9fa616f20fc22547
---
# 014 — Open World: From Tactical Battles to a Living World

## Hook

A tactical battle system is a good starting point for an RPG, but what happens when the game needs to exist beyond the battlefield?

Until now, the focus has been on turn-based combat, where units use abilities and chain effects to defeat their enemies. Moving toward an open world changes the picture considerably. Battles are no longer isolated encounters. They become part of a larger experience in which the player explores, encounters enemies, recruits allies, and prepares for whatever comes next.

This milestone was about making that transition without throwing away the architecture already in place.

## Context

The goal was to evolve the existing battle game into something closer to a full RPG. Instead of treating combat as the entire game, I wanted to establish a world where battles could begin and end naturally as the player explored.

That meant introducing several interconnected systems: party movement, CPU-controlled enemy wandering, dynamic combat engagement, NPC interactions, recruitable characters, enemy respawning, and recovery during exploration.

These systems might sound independent at first, but they all interact with the same units and the same world. A decision made in one system can affect the behaviour of another.

The challenge was not simply implementing more features. It was making them coexist without having to replace the battle architecture.

## Main Story — Extending the Battle Architecture

### Making combat part of the world

The main architectural decision was to extend the existing battle system incrementally rather than rewrite it.

This approach allowed the project to build on existing functionality while introducing the rules needed for exploration. It also meant that assumptions originally made for turn-based combat had to be reconsidered whenever they no longer made sense outside the battlefield.

One important part of that adaptation was using domain events to coordinate behaviour between systems. Rather than relying entirely on direct communication, systems can react to events that represent changes in the game.

This is particularly useful when combat participants change dynamically. Enemies can wander into engagement range and join an ongoing battle, rather than requiring every encounter to be established in advance.

### A different rhythm for enemy AI

CPU-controlled wandering provides a good example of how exploration and combat need different scheduling rules.

During exploration, the wandering Brain executes its loop on a time-based schedule. During battle, it executes after each round instead of running on the same periodic loop.

An important detail here is that turns belong to players, not individual units. By updating wandering behaviour after each round, enemies can move into engagement range and become part of an ongoing encounter. Once engaged, they take their turn when eligible under the battle's turn rules.

This connects the two modes without requiring the world to stop behaving like a world simply because combat has started.

### When general rules need exceptions

One issue became apparent with summoned battle units.

A summon can appear outside the normal engagement range. If the usual proximity-based rules were applied without modification, it could remain outside the battle until it wandered close enough to engage.

That behaviour did not make sense. A unit summoned through a player's ability should not be treated like an unrelated enemy wandering around the map.

The solution was to introduce a special rule: summoned battle units remain engaged until they vanish.

It's a small change, but it illustrates an important design principle. Reusing existing systems is valuable, yet reuse shouldn't mean forcing every entity to follow rules designed for a different purpose.

### Building a world around the battles

Combat and wandering are only part of the open-world foundation.

NPCs provide persistent characters for the player to interact with, while recruitable NPCs allow the world to contribute to the player's party. Respawning keeps the world populated with enemies beyond a single encounter. Recovery can also progress during exploration using world time, rather than relying exclusively on battle turns.

Together, these systems establish the groundwork for a game in which exploration and combat support each other. The world is no longer just a place where battles happen; it can become part of the game's progression and decision-making.

## Key Takeaways

- **Extend working architecture incrementally.** A complete rewrite isn't always necessary when a project grows beyond its original scope.
- **Revisit existing assumptions.** Rules that work during turn-based combat may not work unchanged during exploration.
- **Use events to coordinate interconnected systems.** Domain events provide a way for systems to react to changes without depending entirely on direct communication.
- **Adapt scheduling to the game mode.** Time-based exploration and round-based combat have different rhythms, and the AI needs to respect those differences.
- **Make exceptions explicit.** Summoned units have different expectations from wandering enemies, so their engagement lifecycle needs its own rule.

## What's Next

With the open-world systems implemented, the project now has a foundation for a broader RPG experience.

The next challenge is to give players stronger reasons to explore. Progression systems, quests, and meaningful player goals can build on the world that is now in place, turning movement between encounters into something more purposeful.

The battles remain central to the game, but they no longer have to stand alone. They can become part of a larger adventure.
