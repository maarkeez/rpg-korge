# Game data

The game is defined by TOML files under [`resources`](../../resources). No game data is written in Kotlin: each subdomain loads its own data, and [`SetUpGame`](../../src/commonMain/kotlin/com/mkz/rpg/gameSetup/usecases/commands/SetUpGame.kt) only decides the order in which subdomains are initialised.

## How the game is set up

1. `BattleScene` calls `init()` on each subdomain API. It reads that subdomain's TOML files from the resources bundle (see [Assets](assets.md#how-resources-are-bundled)): a new TOML file is bundled without any change. Reading resources is asynchronous, so it happens before the game is set up.
2. `SetUpGame` publishes one `RequestInitialise<Subdomain>` event per subdomain, in this order, and finally starts exploring and the main quest:

| #  | Event                            | Creates                        | Requires                       |
|----|----------------------------------|--------------------------------|--------------------------------|
| 1  | `RequestInitialiseTerrains`      | Terrains                       | -                              |
| 2  | `RequestInitialiseEffects`       | Effects                        | -                              |
| 3  | `RequestInitialiseLootableItems` | Lootable items                 | Their effects                  |
| 4  | `RequestInitialiseLootTables`    | Loot tables                    | Their lootable items           |
| 5  | `RequestInitialiseAbilities`     | Abilities                      | Their effects                  |
| 6  | `RequestInitialiseUnits`         | Units                          | -                              |
| 7  | `RequestInitialiseLootDrops`     | Loot drops                     | Their units and loot tables    |
| 8  | `RequestInitialisePlayers`       | Players                        | -                              |
| 9  | `RequestInitialiseBattlefield`   | The battlefield                | Its terrains                   |
| 10 | `RequestPlaceBuildings`          | The placed buildings           | Their tiles                    |
| 11 | `RequestCreateQuests`            | The quests, not started yet    | The tiles they name            |
| 12 | `RequestInitialiseBattleUnits`   | The deployed battle units      | Their units, players and tiles |
| 13 | `RequestPlaceNpcs`               | The placed npcs                | Their tiles                    |
| 14 | `RequestStartExploration`        | The exploring party            | The deployed battle units      |
| 15 | `RequestStartMainQuest`          | The main quest, active         | The quests                     |

3. Each `Initialise<Subdomain>` command, as well as `PlaceBuildings`, `PlaceNpcs` and `CreateQuests`, creates all its data synchronously, so the next subdomain can rely on it. It **MUST NOT** publish per-entity requests instead (e.g.: `RequestAbilityCreation`): those would be handled after every remaining initialisation request.

To load a save, `SetUpGame` stops after step 11 (`isLoadingASave`): the battle units, the npcs, the party
and how far each quest went are restored from the save instead, once the first 11 steps are dispatched. See [Save and load](save-and-load.md#loading).

The battlefield tiles are occupied by events: once `BuildingPlaced` is handled, then once `BattleUnitDeployed` is, then once `NpcPlaced` is. So a battle unit on a tile of a building, or an npc on the tile of a battle unit or a building, fails to occupy it (`TileIsOccupied`), and the game fails to start, as it does when the tile of a building, a battle unit or an npc is not occupiable (e.g.: void, out of the battlefield).

Each subdomain follows the same structure:

- `domain`: the `RequestInitialise<Subdomain>` event.
- `usecases.services`: a `<Subdomain>Loader` interface.
- `usecases.commands`: the `Initialise<Subdomain>` command.
- `adapters.resources`: the `Resources<Subdomain>Loader`, built on [`TomlResources.kt`](../../src/commonMain/kotlin/com/mkz/rpg/shared/adapters/toml/TomlResources.kt).
- `adapters.events`: the `OnRequestInitialise<Subdomain>` listener.

An invalid or missing file fails when the resources are read, with the file path in the message.

## Resources

Unless stated otherwise, there is one file per entity and **the file name is the entity id**. Enum values are written by name (e.g.: `"DAMAGE"`).

The files hold no text: the names of the abilities, lootable items, npcs, players, quests, terrains and units, the npc lines, and the description and tasks of each quest, are in [`resources/localization/messages.pot`](../../resources/localization/messages.pot), keyed by the entity id (e.g.: `unit.knight.name`, `npc.elder.line`, `quest.defeat-the-goat-wizard.task.0`), so they can be translated. See [Localization](localization.md).

### `effect/<effect id>.toml`

```toml
[outcome]
type = "DAMAGE"            # DAMAGE, HEAL, RESTORE_MANA, INCREASE_MAXIMUM_HEALTH, INCREASE_MAXIMUM_MANA, TELEPORT, APPLY_EFFECT_TO_ADJACENT_ALLIES, SUMMON, LEARN_ABILITY
damage = 3                 # Only for DAMAGE
# healing = 5              # Only for HEAL
# mana = 5                 # Only for RESTORE_MANA
# maximumHealth = 20       # Only for INCREASE_MAXIMUM_HEALTH
# maximumMana = 20         # Only for INCREASE_MAXIMUM_MANA
# effectId = "..."         # Only for APPLY_EFFECT_TO_ADJACENT_ALLIES
# unitId = "..."           # Only for SUMMON
# abilityId = "..."        # Only for LEARN_ABILITY

[timing]
type = "ON_PLAYER_TURN_STARTED" # IMMEDIATELY, ON_PLAYER_TURN_STARTED, BEFORE_APPLYING_EFFECT, ON_DEFEATED, WHILE_EQUIPPED
duration = 5                   # Only for ON_PLAYER_TURN_STARTED and BEFORE_APPLYING_EFFECT
```

The amount of an outcome (`damage`, `healing`, `mana`, `maximumHealth`, `maximumMana`) goes from 0 to 999. Healing and restoring mana never go above the battle unit's maximum, equipment included.

`INCREASE_MAXIMUM_HEALTH` and `INCREASE_MAXIMUM_MANA` only last while the item granting them is equipped: they must use the `WHILE_EQUIPPED` timing, and `WHILE_EQUIPPED` only accepts these two outcomes. A battle unit applies them while their item is equipped, see [Inventory](inventory.md#equipment).

`LEARN_ABILITY` teaches the battle unit its `abilityId` for good, e.g.: the teleport essence teaches teleport. It must use the `IMMEDIATELY` timing. The ability is not checked to exist, as the effects are created before the abilities. The battle unit adds it at the end of its abilities, ready to be cast, and keeps it when it respawns. It fails when the battle unit already knows the ability or already knows 6 abilities, so an item teaching it can not be consumed then, see [Inventory](inventory.md#learning-an-ability).

### `ability/<ability id>.toml`

```toml
manaCost = 10
cooldown = 0
targeting = "ADJACENT_ENEMY"    # CASTER, ADJACENT_ENEMY, ALL_ADJACENT_ENEMIES, VACANT_TILE_ADJACENT_TO_ANOTHER_BATTLE_UNIT, VACANT_TILE_ADJACENT_TO_CASTER
# summonDuration = 5            # Only when an effect is SUMMON

[[effects]]
effectId = "low-physical-damage"
target = "TARGETED_BATTLE_UNIT" # CASTER, TARGETED_BATTLE_UNIT, TARGETED_TILE, CASTER_TILE, ADJACENT_ALLIES
```

`summonDuration` goes from 1 to 99: how many turns of its player the battle unit the ability deploys (a summon) lasts. An ability with a `SUMMON` effect must have one, and the others can not.

The battle tells the player what an ability does when it is selected, in the ability info under the ability buttons: one
sentence per effect, generated by `AbilityInfoMapper` from its outcome, timing and effect target, e.g.: an `IMMEDIATELY`
`DAMAGE` is "Deals 10 damage.", and an `ON_DEFEATED` `APPLY_EFFECT_TO_ADJACENT_ALLIES` of an `ON_PLAYER_TURN_STARTED`
`DAMAGE` is "When defeated: poisons its adjacent allies (3 damage each turn for 5 turns).". Only the kinds of
effect the abilities use have a sentence: an ability with another kind fails `AbilityInfoResourcesTest` until the kind
gets its case in the mapper and its `ui.abilityInfo.effect.*` text in the catalogs.

### `lootableItem/<lootable item id>.toml`

An item a battle unit can loot. Its effects must exist and suit its type: a consumable's effects can not be `WHILE_EQUIPPED`, and an equippable item's effects must all be `WHILE_EQUIPPED`.

Its `price` is what a merchant charges for it, from 0 to 999. Its sell price, what a merchant pays, is half of it rounded down, and is what the inventory displays, e.g.: `5 G`.

```toml
price = 10
type = "CONSUMABLE"              # CONSUMABLE, EQUIPPABLE

[consumable]                     # Only for CONSUMABLE
effectIds = ["health-potion-heal"]

# [equippable]                   # Only for EQUIPPABLE
# effectIds = ["maximum-health-20"]
# equipmentZone = "HEAD"         # HEAD, BODY, ARMS, LEGS, WEAPON
```

### `lootTable/<loot table id>.toml`

The weighted entries loot drops roll on, shared by every loot drop that lists it. Each entry is a lootable item, which must exist, or nothing. Weights must be greater than 0, and a loot table can not repeat a lootable item nor nothing. The loot table only holds the weights: they are turned into chances when a loot drop is rolled.

```toml
[[entries]]
type = "LOOTABLE_ITEM"           # LOOTABLE_ITEM, NOTHING
lootableItemId = "health-potion" # Only for LOOTABLE_ITEM
weight = 10

[[entries]]
type = "NOTHING"
weight = 5
```

### `unit/<unit id>.toml`

```toml
healthPoints = 100
manaPoints = 30
movementRange = 3
abilities = ["poisoned-sword", "heal"]
startingGold = 20
```

`startingGold` is optional, 0 by default: the gold each battle unit of the unit carries once deployed, never negative (`NegativeStartingGold`). It must also fit the inventory's 99,999, which `Unit` does not know: `InventoryUnitResourcesTest` checks it in CI. Only the battle units of the human player carry gold, see [Inventory](inventory.md#gold).

A unit has 6 `abilities` at most, one per button of the ability bar (`Unit.maximumAbilities()`, `AbilitiesAboveLimit`), checked in CI by `UnitResourcesTest`. Its battle units can learn more abilities, up to the same 6: `BattleUnitDeployment` reads it with `SearchMaximumAbilities` and gives it to the battle unit, which keeps it, and saves it.

### `lootDrop/<unit id>.toml`

The loot tables a unit rolls on when one of its battle units is defeated. **The file name is the unit id**, so a unit has at most one loot drop, and a unit without one drops nothing. The unit and the loot tables must exist, and a loot table can not be repeated.

One lootable item, or nothing, is picked at random from the entries of all its loot tables merged together, in proportion to their weights, and dropped on the tile where the battle unit was defeated. It lies there until an inventory stores it, see [Inventory](inventory.md#looting-from-the-battlefield).

```toml
lootTableIds = ["tier-1"]
```

### `player/<player id>.toml`

```toml
type = "HUMAN"   # HUMAN, CPU
```

### `battlefield/battlefield.toml`

A single file with the terrain id of each tile, one list per row, from top to bottom. All rows must have the same number of columns.

```toml
tiles = [
    ["sand",   "sand",   "void"],
    ["water",  "sand",   "sand"],
]
```

Only sand can be next to another terrain, north, east, south or west, as the terrain transitions are only drawn from sand (`terrain/transitions/sand_to_<terrain>.png`): the game fails to start otherwise (`TileTerrainTransitionNotAllowed`).

A tile next to void on two sides that meet (a corner) or on three sides (a dead end) is not occupiable, whatever its terrain: it is drawn as the edge of the void. Void on one side, or on two opposite sides (a corridor 1 tile wide), leaves it occupiable. So a corridor 1 tile wide can not turn nor end: it turns in a room at least 3 tiles wide, and ends at the edge of the battlefield or in a room.

### `battleUnit/<battle unit id>.toml`

A battle unit deployed when the game starts. The battle units of the human player explore as a party, the first one leading (see [Exploration](exploration.md)). The battles start when the CPU engages, so there is no file for them.

```toml
unitId = "knight"
playerId = "player-one"
row = 6
column = 6
```

### `building/<building id>.toml`

A building standing on several tiles of the battlefield: `row` and `column` are its top-left tile, and `rows` and `columns` its size in tiles. It is no battle unit nor npc: it blocks all its tiles for everyone, nobody can attack it, talk to it nor walk through it, and no terrain effect applies to it.

```toml
row = 17
column = 13
rows = 4
columns = 4
```

The game fails to start when its size is not positive (`BuildingSizeNotPositive`), or when any of its tiles is not vacant (`BuildingTileNotVacant`), e.g.: void or out of the battlefield, or is taken by another building (`TileIsOccupied`). Its image must be its size: [`BuildingImageResourcesTest`](../../src/commonTest/kotlin/com/mkz/rpg/building/adapters/resources/BuildingImageResourcesTest.kt) fails otherwise.

### `npc/<npc id>.toml`

A character standing on a tile of the battlefield, which says its line when the party talks to it. It is no battle unit: it blocks its tile for everyone, nobody can attack it, and no terrain effect applies to it.

```toml
row = 10
column = 2
```

An npc with a `unitId` is recruitable: it joins the party as a battle unit of that unit (e.g.: the viking). The game does not check the unit when it starts: [`NpcUnitResourcesTest`](../../src/commonTest/kotlin/com/mkz/rpg/npc/adapters/resources/NpcUnitResourcesTest.kt) fails when it does not exist.

```toml
row = 12
column = 2
unitId = "viking"
```

An npc with a `stock` is a merchant: once the party talks to it, it shows the lootable items it sells, in the order they are listed (e.g.: the blacksmith). Each item is in unlimited supply, so it is listed once. The game fails to start when an item does not exist (`NpcStockItemNotFound`), when the stock has more than 12 items (`NpcStockTooLarge`), or when it lists an item twice (`NpcStockItemRepeated`).

```toml
row = 14
column = 2
stock = ["health-potion", "mana-potion", "hammer"]
```

### `quest/<quest id>.toml`

A line of tasks, completed one after the other, and the reward granted to the party leader. See [Quests](quests.md). The game needs exactly one `MAIN` quest, and fails to start on a quest with no tasks, an empty reward, negative gold, an empty tile group, a tile that is not vacant or a `requiredDefeats` below 1, on a `SIDE` quest with no `questGiverNpcId`, and on a `MAIN` quest with a `questGiverNpcId` or `repeatable`. The units, npcs and lootable items it names are checked in CI by `QuestResourcesTest`, which also fails on a recruitable npc as a quest giver or a talk task, as recruiting deletes the npc.

```toml
type = "MAIN"                    # MAIN, SIDE
# questGiverNpcId = "farmer"     # Only for SIDE, required: the npc who starts it once the party finishes talking to it
# repeatable = true              # Only for SIDE, false by default: whether a later talk to its quest giver runs it again once completed

[[tasks]]
type = "DEFEAT"                  # DEFEAT, TALK, VISIT
unitId = "goat-wizard"           # Only for DEFEAT
requiredDefeats = 1              # Only for DEFEAT

# [[tasks]]
# type = "TALK"
# npcId = "farmer"               # Only for TALK

# [[tasks]]
# type = "VISIT"
# tiles = [[17, 11], [18, 11]]   # Only for VISIT: a list of [row, column], any of which completes it

[reward]
gold = 150                       # Optional, 0 by default
# lootableItemIds = ["hammer"]   # Optional, none by default
```

The shipped side quests are examples of each: [`bats-in-the-meadow`](../../resources/quest/bats-in-the-meadow.toml) counts defeats then asks to talk, [`the-elders-errand`](../../resources/quest/the-elders-errand.toml) asks to visit some tiles and grants an item, and [`rats-in-the-fields`](../../resources/quest/rats-in-the-fields.toml) is repeatable.

## How to add a unit

1. Add `resources/unit/<unit id>.toml`, and its name to `resources/localization/messages.pot` as `unit.<unit id>.name`.
2. Add its images, following the [asset conventions](assets.md): `unit/<unit id>.png` and `unit/<unit id>_portrait.png`.
3. Optionally, deploy it in the battle with a `resources/battleUnit/<battle unit id>.toml` file.

## How to add a building

1. Add `resources/building/<building id>.toml`, on vacant tiles, where no battle unit is deployed and no npc stands.
2. Add its image, following the [asset conventions](assets.md): `building/<building id>.png`, of 16 pixels per tile.

## How to add an npc

1. Add `resources/npc/<npc id>.toml`, on a vacant tile, where no battle unit is deployed and no building stands. A merchant only sells [lootable items that exist](#how-to-add-a-lootable-item).
2. Add its name and line to `resources/localization/messages.pot`, as `npc.<npc id>.name` and `npc.<npc id>.line`.
3. Add its images, following the [asset conventions](assets.md): `npc/<npc id>.png` and `npc/<npc id>_portrait.png`. A recruitable npc needs none: it is displayed with the images of its unit, which [is added](#how-to-add-a-unit) instead.

## How to add a quest

1. Add the units, npcs and [lootable items](#how-to-add-a-lootable-item) it names, if they do not exist yet.
2. Add `resources/quest/<quest id>.toml`. See [`quest/<quest id>.toml`](#quest-quest-idtoml).
3. Add its texts to `resources/localization/messages.pot`: `quest.<quest id>.name`, `quest.<quest id>.description`, and `quest.<quest id>.task.<index>` for each task, from 0, as a whole sentence, e.g.: `Defeat the Goat Wizard`.

## How to add a lootable item

1. Add the effects it applies, if they do not exist yet: `resources/effect/<effect id>.toml`.
2. Add `resources/lootableItem/<lootable item id>.toml`, and its name to `resources/localization/messages.pot` as `lootableItem.<lootable item id>.name`.
3. Add its image, following the [asset conventions](assets.md): `lootableItem/<lootable item id>.png`.

## How to add a loot table

1. Add the lootable items it drops, if they do not exist yet. See [How to add a lootable item](#how-to-add-a-lootable-item).
2. Add `resources/lootTable/<loot table id>.toml`.

## How to give a unit a loot drop

1. Add the loot tables it rolls on, if they do not exist yet. See [How to add a loot table](#how-to-add-a-loot-table).
2. Add `resources/lootDrop/<unit id>.toml`.

Abilities and effects are added the same way, with their TOML file next to their icon. No Kotlin change is needed,
unless an ability uses a kind of effect the ability info has no sentence for, see
[`ability/<ability id>.toml`](#abilityability-idtoml).
