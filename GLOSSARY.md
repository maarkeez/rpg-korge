# RPG

A turn-based RPG where a party explores the battlefield and fights battles on its tiles.

## Language

### Ability

**Ability**:
An action a battle unit can cast during a battle, defined once and shared by every battle unit that knows it.
_Avoid_: Spell, skill, attack, move

**Mana cost**:
The mana a battle unit spends to cast an ability.
_Avoid_: Cost

**Cooldown**:
The wait an ability imposes after a cast before it can be cast again.
_Avoid_: Cool down, recharge

**Summon duration**:
How long the battle unit an ability summons stays on the battlefield.
_Avoid_: Turns duration, deployment duration

**Ability effect**:
One effect of an ability, together with its effect target.
_Avoid_: Effect spec

#### Targeting

**Targeting**:
An ability's rule for where it can be cast.
_Avoid_: Target

**Caster targeting**:
Targeting where the ability can only be cast on its caster.
_Avoid_: Self

**Adjacent enemy**:
Targeting where the ability is cast on one enemy adjacent to the caster.

**All adjacent enemies**:
Targeting where the ability is cast on every enemy adjacent to the caster at once.

**Vacant tile adjacent to the caster**:
Targeting where the ability is cast on a vacant tile adjacent to the caster.
_Avoid_: Vacant tile adjacent to self, empty tile

**Vacant tile adjacent to another battle unit**:
Targeting where the ability is cast on a vacant tile adjacent to any battle unit other than the caster, ally or enemy.
_Avoid_: Vacant tile adjacent to any battle unit, empty tile

#### Effect target

**Effect target**:
Who or what an ability effect lands on.
_Avoid_: Target expression, target

**Caster**:
The effect target that is the battle unit casting the ability.

**Targeted battle unit**:
The effect target that is a battle unit on a tile the cast is aimed at.
_Avoid_: Selected target

**Targeted tile**:
The effect target that is the vacant tile the cast is aimed at.
_Avoid_: Selected tile

**Caster tile**:
The effect target that is the tile the caster stands on.

**Adjacent allies**:
The effect target that is the caster's living allies on adjacent tiles, not including the caster.
_Avoid_: Nearby allies

### Battle

**Battle**:
A fight on the battlefield between the battle units of two players, played in rounds.
_Avoid_: Combat, fight

**In the battle**:
Said of a battle unit taking part in a battle.
_Avoid_: Engaged

**Out of the battle**:
Said of a battle unit not taking part in the battle in progress.

**Join**:
A battle unit going into a battle in progress.
_Avoid_: Re-engage, update the engagement

**Leave**:
A battle unit going out of a battle in progress.

**Engaging player**:
The player whose battle units start a battle.

**Engaged player**:
The player a battle is started against.

#### Rounds and player turns

**Round**:
One pass through the player turns of every player in a battle.
_Avoid_: Battle round

**Player turn**:
A player's part of a round.
_Avoid_: Turn

**Turn order**:
The order in which the players of a battle take their player turns.
_Avoid_: Turn queue

**Current player**:
The player whose player turn it is.
_Avoid_: Current player turn

#### How a battle ends

**Disengaged**:
Said of a battle with fewer than two of its players left with battle units in it.

**Finished battle**:
A battle that is over, with or without a winner.

**Player defeat**:
A player losing all their battle units, which gives their enemy the victory.
_Avoid_: Loss

**Victory**:
The end of the game, when only one player is left after the defeat of the other.
_Avoid_: Win, game over

**Winner**:
The player who gets the victory.

### Battle unit

**Battle unit**:
A player's piece on the battlefield, deployed from a unit, in a battle or not.
_Avoid_: Unit, character

**Ally**:
Another battle unit of the same player.

**Enemy**:
A battle unit of another player.

**Deploy**:
A battle unit being put on the battlefield for the first time.

**Defeated**:
Said of a battle unit with no health left.
_Avoid_: Dead, killed, KO

**Respawn**:
A defeated battle unit coming back on the battlefield.
_Avoid_: Revive

#### Summons

**Summon**:
A battle unit an ability deploys for its summon duration.
_Avoid_: Summoned battle unit, deployed unit

**Remaining summon duration**:
How much of its summon duration a summon has left.
_Avoid_: Remaining turns

**Vanish**:
A summon going off the battlefield with no defeat.
_Avoid_: Despawn

#### Health and mana

**Health**:
What a battle unit loses to damage, and is defeated without.
_Avoid_: Health points, HP

**Mana**:
What a battle unit spends to cast abilities.
_Avoid_: Mana points, MP

**Remaining health**:
The health a battle unit has left.

**Remaining mana**:
The mana a battle unit has left.

**Maximum health**:
The most health a battle unit can have, equipped effects included.

**Maximum mana**:
The most mana a battle unit can have, equipped effects included.

**Base maximum health**:
A battle unit's maximum health without equipped effects.

**Base maximum mana**:
A battle unit's maximum mana without equipped effects.

**Damage**:
The health a battle unit loses to an effect.

**Heal**:
A battle unit getting health back from an effect.

**Replenish**:
A battle unit getting mana back when its player turn starts.

**Recover**:
A CPU battle unit out of the battle getting back a share of its maximum health and mana on each wandering, in sight or
not.

#### Moving

**Move**:
A battle unit going from its tile to another: a walk, a swap or a teleport.

**Walk**:
A battle unit going along a path, in a battle or not.

**Step**:
One tile of a walk.

**Teleport**:
A battle unit going to a tile without walking there.

#### Actions

**Action**:
A step or a cast a battle unit has each player turn.
_Avoid_: Turn action

**Maximum steps**:
The steps a battle unit has each player turn.
_Avoid_: Movement range

**Remaining steps**:
The steps a battle unit has left this player turn.

**Remaining casts**:
The casts a battle unit has left this player turn.

#### Casting

**Cast**:
A battle unit using an ability, aimed at a tile or a cast group.

**Cast group**:
The tiles one cast is aimed at together.

**Known ability**:
An ability a battle unit can cast, from its unit or learned.

**Learned ability**:
A known ability gained during the game, kept on respawn.

**Maximum abilities**:
The most abilities a battle unit can know.

**Remaining cooldown**:
How much of a cooldown is left on a known ability.
_Avoid_: Cooldown turns left

#### Effects on a battle unit

**Receive**:
A battle unit getting an effect.

**Ongoing effect**:
An effect a battle unit has received and that has not finished.

**Equipped effect**:
An effect that lasts while its item is equipped.

**Remaining duration**:
How much of its duration an ongoing effect has left.
_Avoid_: Turns left

### Battlefield

**Battlefield**:
The grid of tiles the game is played on, in battles and while exploring.
_Avoid_: Map

**Tile**:
One square of the battlefield, with a terrain and at most one occupant.
_Avoid_: Cell, square, battlefield tile

**Position**:
The row and column of a tile.
_Avoid_: Coordinates

**Adjacent**:
Said of two tiles sharing a side: north, east, south or west, never diagonally.
_Avoid_: Next to, neighbour, nearby

**Terrain transition**:
The border drawn on a tile where its terrain meets a different terrain on an adjacent tile.
_Avoid_: Border, edge

**Terrain effect**:
The effect a terrain has on the occupant standing on it.

#### Occupants

**Occupant**:
What stands on a tile, keeping anything else off it: a battle unit, an npc or a building.

**Occupy**:
An occupant taking a tile.
_Avoid_: Update occupancy

**Occupied**:
Said of a tile with an occupant.

**Occupiable**:
Said of a tile an occupant can stand on, given its terrain and the terrains around it.
_Avoid_: Walkable, can be occupied

**Vacant**:
Said of an occupiable tile with no occupant.
_Avoid_: Empty, free

**Remove**:
An occupant going off the battlefield.
_Avoid_: Vacate

**Swap**:
Two occupants exchanging their tiles at once.

#### Walking

**Path**:
The tiles walked from one tile to another.

**Walking distance**:
How many tiles are walked from one tile to another.
_Avoid_: Distance

### Building

**Building**:
An occupant standing for the whole game on a block of tiles, such as the fortress.
_Avoid_: Structure, obstacle

**Place**:
A building being put on its tiles when a game is set up.
_Avoid_: Initialise, deploy, build, spawn

**Top-left tile**:
The tile a building is placed from.

**Size**:
The rows and columns of tiles a building covers.
_Avoid_: Dimensions

### CPU battle brain

**CPU battle brain**:
What decides what the CPU player's battle units do: what they cast and where they walk in a battle, when they engage,
and where they step while wandering.
_Avoid_: AI

#### Decisions

**Decision**:
A cast or a walk the CPU battle brain chooses for one of its battle units, and waits for before choosing the next.
_Avoid_: Action

**Pending**:
Said of a CPU battle unit the CPU battle brain has still to decide for this player turn.

**Done**:
Said of a CPU battle unit the CPU battle brain has nothing left to decide for this player turn.
_Avoid_: Managed

#### Utility

**Utility**:
How much an option is worth to the CPU battle brain.
_Avoid_: Score

**Option**:
A cast or a tile the CPU battle brain weighs the utility of.
_Avoid_: Candidate

**Minimum utility gain**:
How much more than doing nothing an option must be worth for the CPU battle brain to take it.

**Cast kind**:
How the CPU battle brain sorts an ability by its effects: damage, heal, summon or other.
_Avoid_: Attack, deploy

**Healing need**:
The share of its maximum health a battle unit has lost.

**Tiles apart**:
How many tiles separate two tiles along rows and columns, whatever stands between them.
_Avoid_: Manhattan distance, tile distance

#### Engaging

**Engage**:
CPU battle units starting a battle against the enemies within their reach.
_Avoid_: Combat

**Reach**:
How far a CPU battle unit walks to an enemy to engage it.
_Avoid_: Engagement distance

**Within reach**:
Said of an enemy a CPU battle unit can walk to within its reach.

#### Wandering

**Territory**:
The tiles around its home a CPU battle unit wanders on, never on harmful terrain.

**Distance from home**:
How many tiles a CPU battle unit walks from its home to a tile, around void and harmful terrain, through other battle
units.

**Rest**:
A wandering CPU battle unit staying where it is instead of stepping.

**Return**:
A CPU battle unit walking back home after leaving its territory, without resting.
_Avoid_: Head home

### CPU wander brain

**CPU wander brain**:
What walks the CPU player's battle units around their homes while they are out of a battle, and brings the defeated ones
back to them.
_Avoid_: AI

#### Home

**Home**:
The tile a CPU battle unit was deployed on, which it wanders around and respawns on.

**Claim**:
A CPU battle unit taking the tile it is deployed on as its home, for the whole game.

#### Wandering

**Wandering**:
One pass in which the CPU battle units out of a battle wander, one after another.

**Wander**:
A CPU battle unit in sight resting or taking one step around its home during a wandering.

**In sight**:
Said of a CPU battle unit the player may see around the party leader, the only ones that wander.
_Avoid_: Visible, on screen

#### Respawning

**Awaiting respawn**:
Said of a defeated CPU battle unit counting the party steps until it respawns on its home.

**Respawn steps**:
The party steps a defeated CPU battle unit waits before it respawns.

### Effect

**Effect**:
What a source applies to a recipient: an outcome and its timing, defined once and shared by every source that applies it.
_Avoid_: Status, buff, debuff

**Outcome**:
What an effect does to its recipient, such as damage, heal, restore, teleport or summon.
_Avoid_: Effect type

**Amount**:
How much an outcome does, such as the 15 damage of Cleave or the 10 mana of a mana potion.
_Avoid_: Power, points, health points, mana points

**Restore**:
A battle unit getting mana back from an effect.
_Avoid_: Increase mana

#### Applying

**Apply**:
A source giving an effect to a recipient.

**Source**:
What applies an effect: a battle unit casting an ability, a terrain, or an item consumed or equipped.

**Recipient**:
The battle unit or tile an effect is applied to.
_Avoid_: Target, effect target

**Destination**:
The tile a teleport takes its recipient to.

#### Timing

**Timing**:
When the outcome of an applied effect triggers.
_Avoid_: Application

**Trigger**:
The outcome of an effect taking place on its recipient.
_Avoid_: Apply, tick

**Immediately**:
The timing of an effect whose outcome triggers as soon as it is applied.

**On player turn started**:
The timing of an effect whose outcome triggers each time its recipient's player turn starts, for its duration.
_Avoid_: On turn started

**On defeated**:
The timing of an effect whose outcome triggers when its recipient is defeated.

**While equipped**:
The timing of an effect whose outcome lasts while the item that applied it is equipped.

**Duration**:
How many player turns an effect timed on player turn started triggers for.

### Game setup

**Game**:
One whole play, from when it is started new to its victory, which can be saved and loaded again.
_Avoid_: Playthrough, run

**Game setup**:
Getting a game ready to play, new or loaded.
_Avoid_: Battle setup

### Inventory

**Inventory**:
The stored items, equipped items and gold of a party member.
_Avoid_: Bag

**Store**:
An inventory taking an item into one of its storage spaces, whether looted, bought, given or granted.
_Avoid_: Carry, hold

**Stored item**:
An item taking a storage space of an inventory, equipped or not.

**Storage space**:
The place one stored item takes in an inventory.
_Avoid_: Room, slot, capacity

**Maximum storage space**:
How many items an inventory can store.

**Inventory full**:
Said of an inventory with no storage space left.

**Discard**:
A party member removing a stored item from the game.

**Refusal**:
Why a party member can not buy, sell or give right now.

#### Equipment

**Equip**:
A party member putting a stored item in its equipment zone, for its effects to apply while it stays there.

**Unequip**:
A party member taking an equipped item out of its equipment zone, keeping it stored.

**Equipped item**:
A stored item in its equipment zone, at most one per zone.

**Consume**:
A party member using up a stored consumable, which applies its effects.

#### Gold

**Gold**:
The money a party member carries in its inventory.
_Avoid_: Money, purse

**Maximum gold**:
The most gold an inventory can carry.
_Avoid_: Cap, limit

**Room**:
How much more gold an inventory can carry before its maximum gold.

**Gold full**:
Said of an inventory at its maximum gold.

**Grant**:
A party member being given the gold and items of a reward, which can not fail.
_Avoid_: Pay

#### Trading

**Trade**:
A party member buying or selling with the merchant the party talks to.

**Buy**:
A party member paying the price of an item the merchant sells, to store it.

**Sell**:
A party member handing a stored item to the merchant for its sell price.

#### Giving

**Give**:
A party member handing a stored item or gold to another party member, while exploring.

**Giver**:
The party member who gives.

**Receiver**:
The party member a give goes to.

### Localization

**Localization**:
The messages of the game in every language it can be played in, and the selected language.

#### Languages

**Language**:
A language the game can be played in, one per catalog, known by its code, such as es for Spanish.

**Source language**:
English, the language the game is written in, which every other catalog translates.
_Avoid_: Default language, base language

**Selected language**:
The language the game shows its messages in, English until the player selects another for the session.
_Avoid_: Picked language, current language, locale

**Language name**:
The name of a language in that same language, such as Español, which names it in the language list.

#### Catalogs

**Catalog**:
The messages of one language, such as the Spanish one.
_Avoid_: PO file, translation

**Source catalog**:
The catalog of the source language, which holds every message.
_Avoid_: Template

#### Messages

**Message**:
A piece of the game's text players read, found by its key in every catalog.
_Avoid_: Text, string, entry

**Key**:
What identifies a message in every catalog, such as ui.shop.notEnoughGold.
_Avoid_: Context, id

**Translation**:
A message in a language other than the source language, such as "No hay suficiente oro" for "Not enough gold".

**Untranslated**:
Said of a message with no translation in a catalog, shown in the source language instead.
_Avoid_: Missing translation, fallback

**Translate**:
To get a message in the selected language, with its placeholders filled in.

**Placeholder**:
A named gap in a message, such as {turns} in "Vanishes in {turns} turns", filled in when it is translated.
_Avoid_: Variable, parameter

#### Plurals

**Plural form**:
One version of a message for some counts, such as "Vanishes in 1 turn" and "Vanishes in 3 turns".

**Plural rule**:
How a language picks the plural form of a message for a count: English has one for 1 and another for every other count.
_Avoid_: Plural forms

**Count**:
The number a plural form is picked for, such as the 3 of "Vanishes in 3 turns".

### Lootable item

**Lootable item**:
Something a party member stores in its inventory to consume or equip, defined once and shared by every inventory that
stores it. Item for short.
_Avoid_: Loot

**Loot type**:
Whether a lootable item is a consumable or an equippable.
_Avoid_: Item type, kind

**Consumable**:
A lootable item used up once to apply its effects, such as a health potion.

**Equippable**:
A lootable item whose effects last while it is equipped in its equipment zone, such as a hammer.
_Avoid_: Equipment, gear, wearable

**Equipment zone**:
The place on a party member an equippable is equipped in: head, body, arms, legs or weapon.
_Avoid_: Slot

#### Price

**Price**:
The gold a merchant charges for a lootable item.
_Avoid_: Cost, value

**Sell price**:
The gold a merchant pays for a lootable item.
_Avoid_: Resale value

### Loot drop

**Loot drop**:
The loot tables a unit rolls on when one of its battle units is defeated, at most one per unit.
_Avoid_: Drop table

**Roll**:
Picking at random, by weight, an item or nothing from the loot tables of a loot drop.

#### Dropped items

**Drop**:
A lootable item being left on a tile, by a defeated battle unit or from a reward with no storage space left for it.

**Dropped item**:
A lootable item lying on a tile until a party member loots it.
_Avoid_: Loot

**Find**:
A battle unit walking or teleporting onto a tile with dropped items.

**Loot**:
A party member storing a dropped item from a tile it walks or teleports onto, which takes the item off the tile.
_Avoid_: Pick up
