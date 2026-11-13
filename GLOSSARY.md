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
A battle that is over.

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

**Defeat**:
A battle unit becoming defeated: its health going from some left to none. It happens once until the battle unit
respawns, as a defeated battle unit takes no more damage.
_Avoid_: Death, kill

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
An ability a battle unit can cast: a starting ability of its unit, or a learned ability.

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

**Stack**:
An effect received again while it is already ongoing, becoming another ongoing effect with its own remaining duration,
such as two venoms each dealing their damage each player turn.
_Avoid_: Refresh

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
_Avoid_: Walkable

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
One whole play, from when it is started new until its game over, which can be saved and loaded again.
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

### Loot table

**Loot table**:
A list of weighted entries that loot drops roll on, shared by every loot drop that lists it.

**Entry**:
One line of a loot table: a lootable item or nothing, with its weight.
_Avoid_: Outcome, row

**Weight**:
How much of a roll an entry gets, compared with the other entries rolled with it.
_Avoid_: Chance, probability, odds

**Nothing**:
An entry that leaves no item when it is rolled.
_Avoid_: Empty, no drop

### Npc

**Npc**:
A character standing on a tile of the battlefield, whom the party talks to, and who is no battle unit: nobody can attack it.
_Avoid_: NPC, villager, townsfolk

**Line**:
What an npc says in one talk: its own line, or a quest line.
_Avoid_: Dialog, text, quote

**Own line**:
The line an npc says when no quest has a line for it. A quest giver has none.
_Avoid_: Default line, greeting

**Place**:
An npc being put on its tile when a game is set up.
_Avoid_: Initialise, spawn

#### Recruiting

**Recruit**:
An npc leaving its tile to join the party as a battle unit of its unit, once the talk with it is finished.
_Avoid_: Hire, join

**Recruitable**:
Said of an npc with a unit to be recruited as.

#### Merchants

**Merchant**:
An npc the party members buy the items of its stock from, and sell their stored items to.
_Avoid_: Shopkeeper, vendor, trader

**Stock**:
The lootable items a merchant sells, in the order they are shown, never running out.
_Avoid_: Wares, goods, inventory

### Party

**Party**:
The human player's battle units that explore the battlefield together, one leading and the rest following, in a battle
or not.

**Party member**:
A battle unit of the party. Member for short.

**Leader**:
The party member in front, the first in the party order, whom the player steers.

**Follower**:
A party member other than the leader, who follows the one ahead of it in the walking order.

**Party order**:
The order of the party members, set in the formation, which decides who leads.

**Walking order**:
The order in which the party members follow one another, the leader first.
_Avoid_: Line, line order

**Join the party**:
A battle unit becoming the last party member, such as a recruited npc.

**Leave the party**:
A party member going out of the party when it is defeated.

**Game over**:
The end of the game, when the last party member leaves the party.
_Avoid_: Player defeat, loss

#### Exploring

**Exploring**:
Said of the party out of the battles while time goes on.

**Second**:
The beat of time while exploring, as the round is in a battle.

**Party step**:
The party moving one tile on, the leader first and each follower onto the tile the one ahead of it left.
_Avoid_: Party walk

**Standing still**:
Said of a battle unit that has not moved during the current second.

**Interrupt**:
A battle starting, which stops the party exploring until it finishes.
_Avoid_: Pause

**Resume**:
The party exploring again once the battle that interrupted it finishes.

#### Time stopped

**Time stopped**:
Said of the party out of the battles while it talks, is in the formation, is paused or reads a quest.

**Talk**:
The party listening to the line of an npc adjacent to the leader, with time stopped, until the player finishes it.

**Formation**:
Where the player sets the party order, with time stopped.

**Pause**:
The player stopping time from the pause screen, such as to save the game.

### Player

**Player**:
One of the two sides of the game, human or CPU, each with its battle units.
_Avoid_: Side

**Human player**:
The player a person plays, whose battle units explore as the party.
_Avoid_: Party player

**CPU player**:
The player the game plays itself.
_Avoid_: AI, computer

**Opponent**:
The other player of the game.
_Avoid_: Enemy, enemy player

**CPU battle unit**:
A battle unit of the CPU player.

### Quest

**Quest**:
A series of tasks the party completes one after the other, the last one earning a reward.
_Avoid_: Mission

**Main quest**:
The one quest that starts once a party member walks or teleports onto one of its start tiles, which runs once and can
not be abandoned.

**Start tiles**:
The tiles of the main quest that start it once a party member walks or teleports onto one of them, such as the
entrance of the village.

**Side quest**:
A quest that starts when the party finishes talking to its quest giver.

**Quest giver**:
The npc whose talk starts a side quest.
_Avoid_: Giver

**Repeatable**:
Said of a side quest its quest giver can start again once it is completed.

**Reward**:
The gold and lootable items a completed quest grants to the party leader.

#### Progress

**Progress**:
How far a quest went: its status, its current task, its defeats counted, and whether it is pinned.

**Start**:
A quest becoming active, on its first task.

**Active**:
Said of a quest started and not yet completed.

**Completed**:
Said of a quest whose last task is done.

**Abandon**:
The player giving up an active side quest, which goes back to not started with its progress lost.

#### Tasks

**Task**:
One stage of a quest, done once the party does what it asks.

**Current task**:
The task an active quest asks for now.

**Defeat task**:
A task done once enough battle units of one unit, of the CPU player, are defeated.
_Avoid_: Defeat units

**Required defeats**:
How many defeats a defeat task needs, such as the 3 bats of Bats in the Meadow.
_Avoid_: Count

**Defeats counted**:
How many of its required defeats the current task has counted so far.
_Avoid_: Progress, task progress

**Talk task**:
A task done once the party finishes the talk in which its npc says its talk task line.
_Avoid_: Talk to npc

**Visit task**:
A task done once a party member walks or teleports onto one of its tiles.
_Avoid_: Reach, touch

#### Lines

**Quest line**:
A line a quest has for its quest giver, or for the npc of one of its talk tasks.
_Avoid_: Dialog, questline

**Start line**:
The quest line a quest giver says in the talk that starts its side quest.
_Avoid_: Offer line, intro

**Reminder line**:
The quest line a quest giver says while its side quest is active and the talk does none of its talk tasks.
_Avoid_: In progress line, active line

**Talk task line**:
The quest line an npc says in the talk that does its talk task.
_Avoid_: Turn-in line

**Completed line**:
The quest line a quest giver says once its side quest, when not repeatable, is completed.
_Avoid_: Thanks line

**Start again line**:
The quest line a quest giver says in the talk that starts its completed repeatable quest again.
_Avoid_: Repeat line

#### Pins and markers

**Pin**:
The player choosing the active side quest the tracker shows.

**Pinned quest**:
The quest the tracker shows: the pinned side quest, or else the main quest once started.
_Avoid_: Followed quest, tracked quest

**Quest marker**:
The sign over an npc's head that it has a quest to start, or is the npc of a current talk task.

### Save

**Save**:
A game as it was saved into a save slot, to be loaded again.
_Avoid_: Saved game, savegame

**Save slot**:
One of the 3 places a save is kept, empty or holding one save.

**Summary**:
What a save slot shows of its save: when it was saved, its leader and the party size.
_Avoid_: Header, metadata

**Snapshot**:
The runtime state of one subdomain as kept in a save, such as the health and mana of every battle unit.
_Avoid_: Saved state

#### Saving and loading

**Overwrite**:
Saving into a save slot that holds a save, in place of it, once confirmed.
_Avoid_: Replace

**Load**:
Taking a save back into play, the game as it was when saved.
_Avoid_: Restore

#### Compatibility

**Save version**:
The version of the shape of the saves the game writes; a save of another one is incompatible.
_Avoid_: Format version, schema version

**Incompatible save**:
A save that can't be loaded, as it was written by another save version or names what the game no longer has; it can
only be deleted or overwritten.
_Avoid_: Corrupt save, broken save

### Screen

**Game screen**:
Where a game is played, from a new game or a loaded save until the player quits: the banner, the battlefield, the HUD
and the call to action.
_Avoid_: Battle scene

**Main menu**:
Where the player plays a new game, loads a save, builds a world or selects a language.

**New game**:
A game played from its start, not loaded from a save.

**Pause screen**:
Where the player, with time stopped, resumes, saves the game or quits.

**Quit**:
The player going back to the main menu, from the pause screen, losing what was played since the last save, or after
the game over.

#### The battlefield on the screen

**Camera**:
What part of the battlefield is visible, 6 by 6 tiles, trailing the leader while exploring.
_Avoid_: View

**Viewport**:
Where and at what size the game screen draws what the camera shows.

**Building piece**:
The part of a building's image drawn on one of its tiles.

**Banner**:
The line at the top of the game screen telling whether the party is exploring, or the round and player turn of a battle,
and announcing a battle, its end and the game over.
_Avoid_: Header, battle info

#### Selection

**Selection**:
What the player has selected on the battlefield: nothing, a battle unit, one of its abilities, or a cast to preview.
_Avoid_: Battlefield HUD

**Selected battle unit**:
The battle unit whose tile the player tapped, which the HUD shows.

**Walking range**:
The tiles the selected battle unit can walk to with its remaining steps, in a battle.
_Avoid_: Movement range

**Selected ability**:
An ability the selected battle unit can cast now, chosen by the player to cast it.

**Cast range**:
The tiles the selected ability can be cast on.

**Ability info**:
What an ability does and costs, and why it can not be cast now, when it can not.

**Cast preview**:
The forecast of a cast before the player confirms it: the ability, its caster and the mana spent, and what each
recipient would be left with.
_Avoid_: Attack preview

#### HUD

**HUD**:
The panel under the battlefield showing the selected battle unit and its abilities, a cast preview, or an inventory, in
battles and while exploring.
_Avoid_: Battle HUD

**Battle unit info**:
The part of the HUD showing the selected battle unit: its portrait, name, health, mana, remaining steps and casts,
effects and abilities.
_Avoid_: Unit info

**Inventory button**:
The chest that opens the inventory of the selected party member.
_Avoid_: Bag

**Call to action**:
The place under the HUD, at the player's thumb, holding Finish turn, Cancel and Confirm of a cast, or a button row.

**Button row**:
The buttons of a panel in the call to action, such as Back and Abandon of a quest.
_Avoid_: Action row, actions

**Item buttons**:
What a party member can do with the selected stored item, such as Consume, Give or Discard.
_Avoid_: Item actions

**Virtual pad**:
The arrows the player steers the leader with while exploring.

#### Panels

**Talk panel**:
The panel showing the npc the party talks to: its portrait, name and line, and Buy, Sell and Leave for a merchant.
_Avoid_: Dialog, npc dialog

**Shop**:
The panel where a party member buys from the stock of the merchant the party talks to, or sells its stored items.
_Avoid_: Store, market

**Tracker**:
The capsule naming the current task of the pinned quest while exploring.

**Empty tracker**:
The tracker while a side quest is active, none is pinned and the main quest has not started, which opens the journal
to pin a side quest.

**Journal**:
The list of every quest started, read with time stopped.

**Quest detail**:
One quest in full: its description, current task, tasks done and reward.

**Quest announcement**:
The notice that a quest starts or is completed, shown for a moment without stopping the game.
_Avoid_: Toast

### Shared

**Game data**:
What defines the game and never changes while playing, such as its terrains, units, abilities and quests, read from
its files rather than saved.
_Avoid_: Config, content

**Definition**:
The game data of one thing, such as the knight in `unit/knight.toml`.

#### Bars

**Bar**:
A value out of its maximum, filled in proportion, with the value after it, such as a battle unit's health at 18/30.
_Avoid_: Progress bar, gauge

**Preview bar**:
A bar of what a cast would leave, such as a recipient's health: 30 → 18 in the cast preview, or blinking over its tile.

**Blink**:
Showing and hiding in a loop, to draw the eye: to what a cast would change, or to a battle unit that receives something,
such as an effect, an item or gold, or that joins the party.
_Avoid_: Flash, pulse

#### Buttons

**Disabled**:
Said of a button shown faded, as it can't be tapped now, such as Discard for an equipped item.
_Avoid_: Greyed out, inactive

### Storybook

**Storybook**:
The views of the game shown in isolation, each in its states, without playing up to them.

**Catalogue**:
Every component of the Storybook with its stories, listed by group in the sidebar.

**Component**:
A view of the game in the catalogue, named by its group and title, such as **Screen / Shop**.

**Group**:
The subdomain of a component's view, such as Screen or Shared.

**Story**:
A named state of a component, such as **Screen / Shop › Sell, inventory full**.

**Selected story**:
The story the phone shows.
_Avoid_: Story on show

**Story binding**:
What ties a component to the real view of the game it shows.

**Location**:
The selected story, its changed args and its changed globals, kept in the page URL so that a link opens the same.

**Open**:
Starting the Storybook on what its location asks for.
_Avoid_: Initialise

#### Controls

**Control**:
The editor of an arg or a global, which defines the values it accepts: a boolean, an int, an enum, a pick, a text or a
color.

**Option**:
One of the values an enum or a pick control accepts, such as a lootable item id.

**Value**:
What a control holds: an arg or a global.

**Arg**:
The value of a control of the selected story's component, such as `gold = 120`, which its view is displayed with.

**Global**:
A value shared by every story, such as the language.

**Action**:
A callback made by the view of the selected story, such as `shopSlotSelected(3)`.

**Record**:
Adding an action to the Actions, the newest on top.
_Avoid_: Log

#### Layout

**Sidebar**:
The catalogue on the left, with its search, where a story is selected.

**Phone**:
The 390x844 screen of the game window, in the middle, showing the selected story at its real size.

**Toolbar**:
The globals, on the top right.

**Story info**:
What the selected story and its component are, each with its description.

**Viewport**:
Where and at what size the screen draws the Storybook, zoomed from Fit to 4×.

**Fit**:
The zoom that shows the whole Storybook, as on its 1280x900 window.

### Telemetry

**Product event**:
One fact about how the game is played, such as a quest completed, recorded as it happens.
_Avoid_: Metric, analytics event, tracking event

**Install id**:
The random id a copy of the game keeps on its device, the same in every session.
_Avoid_: Player id, user id, device id

**Session**:
One run of the game, from its launch until it is closed.
_Avoid_: Play session

**Game id**:
The random id a game gets when it is started new, kept in its saves, so a loaded game keeps it.
_Avoid_: Save id, playthrough id

**Consent**:
The player's choice to let the game record product events, made on a version of the privacy notice.
_Avoid_: Opt-in, tracking permission

**Privacy notice**:
What the game tells the player about what it records, why, who receives it and for how long.
_Avoid_: Privacy policy

#### Sending

**Telemetry collector**:
The service the game sends its product events to, which keeps them for the retention period.
_Avoid_: Collector, backend, telemetry server, endpoint, Worker

**Queued product event**:
A product event recorded on the device and not yet accepted by the telemetry collector.
_Avoid_: Pending event, unsent event, buffered event

**Retention period**:
How long the telemetry collector keeps a product event before deleting it.
_Avoid_: TTL, data retention, expiry

### Terrain

**Terrain**:
What a tile is made of, such as sand, grass, water, lava or void.
_Avoid_: Ground, tile type, floor

**Can be occupied**:
Said of a terrain an occupant can stand on, unlike void.
_Avoid_: Occupiable, walkable

#### Terrain effects

**Harmful terrain**:
A terrain whose terrain effect damages the occupant standing on it, such as lava.

**Healing terrain**:
A terrain whose terrain effect heals the occupant standing on it, such as water.

### Unit

**Unit**:
The kind of battle unit a battle unit is deployed from, such as the knight. It sets the base maximum health and mana,
maximum steps, starting abilities and starting gold of its battle units.
_Avoid_: Unit type, class, template, character

**Starting ability**:
An ability every battle unit of a unit knows from its deployment, up to the maximum abilities.
_Avoid_: Unit ability, abilities

**Starting gold**:
The gold each battle unit of a unit carries once deployed.

### World builder

**World**:
What the world builder builds for a game to be played in: for now, a battlefield.
_Avoid_: Map, game, scenario, level

**World builder**:
Where the player builds one world at a time, to export it.
_Avoid_: Map builder, game builder, editor

**Size**:
How many rows and columns of tiles a world has.
_Avoid_: Dimensions

**Resize**:
Changing the size of a world, keeping the tiles that still fit.

#### Painting

**Selected terrain**:
The terrain the player paints tiles with.
_Avoid_: Brush, current terrain

**Paint**:
Giving a tile of the world the selected terrain.
_Avoid_: Draw, set

#### Files

**Export**:
Writing the world as text, in the format a battlefield is loaded from.

**Import**:
Replacing the world with one read from a file.
_Avoid_: Load, parse

**Rejected**:
Said of a paint or an import that leaves the world as it was, as its result would not be a world a game can be played in.
