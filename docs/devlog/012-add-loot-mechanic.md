---
date: 15th October 2026
commit: 17a8e0593454da50bd2bb6814c1ba5ede75cef5d
---
# 012 — Add Loot Mechanic: Turning Enemy Drops into Tactical Decisions

## Hook

What if defeating an enemy wasn't the end of the reward, but the beginning of another tactical decision?

I wanted loot to be more than an item automatically added to the player's collection. In this game, the battlefield is a 2D grid, and positioning is already an important part of combat. By placing dropped items directly on battlefield tiles, I could turn rewards into something the player must actively pursue.

That simple decision connects combat, movement, inventory management, equipment, and character progression into one mechanic.

## Context

Until now, the game has had an effect system that can apply outcomes such as healing. The challenge was to build a loot system on top of the existing architecture without duplicating functionality or making unnecessary changes to established systems.

The goal was to implement a complete gameplay loop: enemies drop items, player-controlled units collect them, and those items can be consumed or equipped.

I deliberately limited the scope to individual inventories for human-controlled battle units. Shops, purchasing, trading between units, and CPU inventories would come later.

## Main Story — Building a Reusable Loot System

### Separating the responsibilities

I introduced four new domains: `LootableItem`, `LootTable`, `LootDrop`, and `Inventory`.

Each has a specific responsibility. `LootableItem` defines the items, including their identifiers, names, images, types, and effects. `LootTable` defines weighted item entries. `LootDrop` determines which tables are associated with each battle unit and resolves the drop. `Inventory` manages the items a unit carries and equips.

This separation allows each part to evolve independently. It also keeps loot configuration in game data rather than hardcoding item definitions and drop probabilities into the implementation.

Consider a simple example. I can reuse an existing `heal` effect to create a new `heal potion`. I can then add that item to a `tier I` loot table and decide which enemies can drop that table. The effect, item, loot table, and enemy assignment are separate pieces that can be recombined without rebuilding the system.

The additional domains and APIs introduce some architectural complexity, but they also provide a flexible foundation for future iteration.

### Resolving the loot drop

A defeated enemy does not automatically produce an item. Its loot configuration references one or more loot tables, whose weighted entries are merged into a single pool.

The system then selects one outcome according to the relative weights. I also added an explicit `nothing` entry, making an empty drop a valid result. At most one item can drop from a defeated unit.

For example, if the merged pool contains an item with weight 20 and a `nothing` entry with weight 80, the item has a 20% chance of being selected, assuming those are the only entries.

Loot tables can be reused across enemies, while the assignment of tables to units remains configurable. The tables themselves are hidden from players; they experience the resulting drops rather than inspecting the underlying probabilities.

### Making loot part of the battlefield

When a battle unit is defeated, the loot system resolves its drop and announces the result. If an item drops, it appears on the tile where the unit was defeated.

I integrated this behavior with the existing battlefield rather than introducing a separate pickup subsystem. Tile occupancy updates trigger collection when a unit steps onto a tile containing loot. Only units with inventories can collect items in this version.

The battle scene timeline also includes the drop and pickup events, allowing the presentation to follow the battle's progression. When multiple items occupy the same tile, the screen displays a box asset instead of attempting to show several item images at once.

The result is a direct connection between combat and movement: collecting a reward requires a unit to reach its location. That can introduce risk and create another decision about where to move.

### Individual inventories and meaningful choices

I considered using a shared party inventory, but chose individual inventories instead. A defeated unit can therefore have consequences beyond losing its combat capabilities: the unit also loses its ability to collect and manage its own items.

Each inventory has a maximum capacity, and equipped items count towards that limit. When an inventory is full, the player chooses an item to discard to make room for a new drop, or leaves the new item behind.

This makes inventory capacity a strategic constraint rather than an invisible storage problem.

Items fall into two categories. Consumables apply their effects when used and are then removed. Equippable items remain in the inventory system while equipped, occupying one of five zones: head, body, arms, legs, or weapon.

I also decided that inventory operations would not consume a turn action. Players can manage their equipment and use items without sacrificing a tactical action to do so.

### Extending the existing effect system

Equipment introduced an important difference in effect behavior. A healing potion applies its effect when consumed, but an equipment bonus must remain active for as long as the item is equipped.

Rather than building a separate effect mechanism, I extended the existing `Effect` domain with a `while equipped` application type and allowed an item to be identified as the source of an effect.

I also added outcomes for increasing maximum health and maximum mana, alongside an increase-mana outcome for consumables.

These changes required the battle unit domain to account for equipped effects when calculating maximum stats. Equipping, unequipping, and swapping items must apply or remove the appropriate effects. When removing equipment lowers a unit's maximum health or mana, its current values must remain within the new limits.

This illustrates why persistent effects need explicit lifecycle rules: adding a bonus is only half the problem. The system must also handle its removal correctly.

## Key Takeaways

- **Reuse existing capabilities.** Building on the effect system avoids duplicating established functionality.
- **Complete a focused gameplay loop first.** Drops, collection, inventory management, consumption, and equipment provide a foundation before shops or trading are introduced.
- **Make configuration composable.** Reusable effects, items, loot tables, and enemy assignments make balancing and iteration more flexible.
- **Define effect lifecycles explicitly.** Persistent equipment bonuses must be applied and removed consistently, especially when maximum stats change.
- **Let inventory rules support the gameplay.** Individual ownership, limited capacity, and free inventory actions create strategic decisions without adding turn costs to routine management.

## What's Next

With the initial loot loop in place, the next challenge is to build on it without losing its flexibility. Shops, purchasing, and CPU inventories remain possible extensions. For now, the priority is to continue developing the combat experience around the systems already in place.