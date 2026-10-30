---
date: 30th October 2026
commit: 3533c152a216f0b053b05913c6894e5318a04a5b
---

# 015 — Game Economy: Gold, One Slice at a Time

## Hook

An economy is huge.

Gold. Prices. Merchants. Wallets. Buying. Selling. Each one leans on the others, and on paper they look like a single
system.

> What is the smallest piece of an economy that could ship on its own?

**An economy is many systems, but each one can ship on its own: a price before a merchant, a merchant before a wallet.**

## Context

Since [the loot mechanic](012-add-loot-mechanic.md), defeated enemies drop potions and hammers on the battlefield. Each
battle unit carries its own bag, with limited space, and equips what it finds. Since [the open world](014-open-world.md),
the party explores and talks to npcs.

But an item had no value beyond its effect. A full bag only offered one way out: discard something.

The goal was to add another layer to the player's strategy. Is a dropped item worth attacking for, or walking over?
Should the party prepare before a battle? Which party member keeps the item, and improves for a while, or for good?

I kept the scope deliberately small:

- One price per item, no prices per merchant.
- The merchant has unlimited gold and unlimited stock. A sold item leaves the game.
- No gold from enemies: the knights start with 20 G, the viking with 100 G, and selling is the only way to earn more.

## Main Story — Four Plans in a Day

My first instinct was a single plan for the whole economy. I dropped it: every decision at once, and nothing playable
for days.

Instead, I wrote four plans and built them all on the 20th of October.

### 1. A price, and nowhere to spend it

The first slice was only a number: a `price` from 0 to 999 in each lootable item's TOML file. The potions are worth
10 G, the hammer 50 G. The inventory shows it next to the name:

```text
Health Potion ........ 10 G
```

No wallet, no merchant. It still changed the full-bag question: one item is now worth five times the other.

I also considered a buying price and a selling price per item. **A single price is enough: buying costs it, selling
pays half, rounded down.** The hammer is bought for 50 G and sold for 25 G.

### 2. A merchant who only shows

The second slice added a merchant who sells nothing yet. Talking to it opens a shop of up to 12 items, laid out like the
inventory. Tapping one shows its name and price.

**A merchant is just an npc with a stock**, the way a recruitable npc is an npc with a unit.

### 3. Buying and selling

The third slice was the real economy, and its biggest decision: where the gold lives.

A shared party wallet is the obvious choice. I dropped it for the same reason I dropped a shared party inventory: **who
carries the gold matters**. Each battle unit keeps its own gold, in its own inventory, and the shop trades with one buyer
at a time:

```text
Knight · 20 G · Bag 11/12
```

The knight can't afford the hammer. The viking can. So the viking buys it, and the viking carries it.

That reaches into battle too: a party member can be defeated, so how to split the loot across the party is a strategy
of its own. Sharing gold may come later, as an option, but not for now.

### 4. Something worth buying

The last slice gave gold a goal: the teleport essence, worth 100 G, which teaches its battle unit teleport for good.

It is a regular consumable with a new `LEARN_ABILITY` effect outcome. The knights already know teleport, so only the
viking can learn it.

### The shop that asked too much

Then I played it.

The first shop had tabs, Close, slots, buyer dots, Cancel and Buy, all at once. Worse, a greyed-out Buy said nothing:
was the knight broke, or the bag full?

Two days later I redesigned it. The merchant's dialog now offers Buy, Sell and Leave, so the shop opens in a single
mode. A disabled button reads its reason: `Not enough gold`, `Bag full`, `Equipped` or `Gold full`. The items the buyer
can't trade are dimmed.

### What is still unfinished

- **The economy isn't balanced.** 10, 50 and 100 G are guesses. Balancing will come as the game's scope grows.
- **No gold from enemies**, and an endless merchant means no scarcity yet.
- **Trading tangled the code.** Inventory now needed the party and the npcs, and the next day I found dependency cycles
  between subdomains.

## Key Takeaways

- **Ownership is strategy.** Gold and items per battle unit turn who carries what into a decision, before and during
  battles.
- **Reuse before inventing.** A merchant is an npc with a stock, the essence a consumable with a new outcome, and the
  shop reuses the inventory's slots.
- **Balance later.** Prices are placeholders until there is enough game to balance them against.

## What's Next

Four slices in a day kept every decision small. But together they left knots behind: inventory, party, npcs and the
battlefield depending on each other in circles.

The next entry is about untangling them.

Small slices keep the game playable. They don't keep the architecture clean on their own.
