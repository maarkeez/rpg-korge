# Game data

The battle is defined by TOML files under [`resources`](../../resources). No game data is written in Kotlin: each subdomain loads its own data, and [`SetupBattle`](../../src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt) only decides the order in which subdomains are initialised.

## How the battle is set up

1. `BattleScene` calls `init()` on each subdomain API. It reads that subdomain's TOML files. Reading resources is asynchronous, so it happens before the battle is set up.
2. `SetupBattle` publishes one `RequestInitialise<Subdomain>` event per subdomain, in this order, and finally starts exploring:

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
| 10 | `RequestInitialiseBattleUnits`   | The deployed battle units      | Their units, players and tiles |
| 11 | `RequestInitialiseNpcs`          | The npcs on the battlefield    | Their tiles                    |
| 12 | `RequestStartExploration`        | The exploring party            | The deployed battle units      |

3. Each `Initialise<Subdomain>` command creates all its data synchronously, so the next subdomain can rely on it. It **MUST NOT** publish per-entity requests instead (e.g.: `RequestAbilityCreation`): those would be handled after every remaining initialisation request.

The battlefield tiles are occupied by events: once `BattleUnitDeployed` is handled, then once `NpcPlaced` is. So an npc on the tile of a battle unit fails to occupy it (`TileIsNotVacant`), and the game fails to start, as it does when the tile of a battle unit or an npc can not be occupied (e.g.: void, out of the battlefield).

Each subdomain follows the same structure:

- `domain`: the `RequestInitialise<Subdomain>` event.
- `usecases.services`: a `<Subdomain>Loader` interface.
- `usecases.commands`: the `Initialise<Subdomain>` command.
- `adapters.resources`: the `Resources<Subdomain>Loader`, built on [`TomlResources.kt`](../../src/commonMain/kotlin/com/mkz/rpg/shared/adapters/toml/TomlResources.kt).
- `adapters.events`: the `OnRequestInitialise<Subdomain>` listener.

An invalid or missing file fails when the resources are read, with the file path in the message.

## Resources

Unless stated otherwise, there is one file per entity and **the file name is the entity id**. Enum values are written by name (e.g.: `"DECREASE_HEALTH"`).

### `effect/<effect id>.toml`

```toml
[outcome]
type = "DECREASE_HEALTH"   # DECREASE_HEALTH, INCREASE_HEALTH, INCREASE_MANA, INCREASE_MAXIMUM_HEALTH, INCREASE_MAXIMUM_MANA, TELEPORT, APPLY_EFFECT_ON_NEARBY_ALLIES, DEPLOY_BATTLE_UNIT
damage = 3                 # Only for DECREASE_HEALTH
# healing = 5              # Only for INCREASE_HEALTH
# mana = 5                 # Only for INCREASE_MANA
# healthPoints = 20        # Only for INCREASE_MAXIMUM_HEALTH
# manaPoints = 20          # Only for INCREASE_MAXIMUM_MANA
# effectId = "..."         # Only for APPLY_EFFECT_ON_NEARBY_ALLIES
# unitId = "..."           # Only for DEPLOY_BATTLE_UNIT

[application]
type = "ON_TURN_STARTED"   # IMMEDIATELY, ON_TURN_STARTED, BEFORE_APPLYING_EFFECT, ON_DEFEATED, WHILE_EQUIPPED
duration = 5               # Only for ON_TURN_STARTED and BEFORE_APPLYING_EFFECT
```

The amounts (`damage`, `healing`, `mana`, `healthPoints`, `manaPoints`) go from 0 to 999. Healing and restoring mana never go above the battle unit's maximum, equipment included.

`INCREASE_MAXIMUM_HEALTH` and `INCREASE_MAXIMUM_MANA` only last while the item granting them is equipped: they must use the `WHILE_EQUIPPED` application, and `WHILE_EQUIPPED` only accepts these two outcomes. A battle unit applies them while their item is equipped, see [Inventory](inventory.md#equipment).

### `ability/<ability id>.toml`

```toml
name = "Skull"
cost = 10
cooldown = 0
targeting = "ADJACENT_ENEMY"   # SELF, ADJACENT_ENEMY, ALL_ADJACENT_ENEMIES, VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, VACANT_TILE_ADJACENT_TO_SELF
# turnsDuration = 5            # Only when an effect is DEPLOY_BATTLE_UNIT

[[effects]]
effectId = "low-physical-damage"
target = "SELECTED_TARGET"     # CASTER, SELECTED_TARGET, SELECTED_TILE, CASTER_TILE, NEARBY_ALLIES
```

`turnsDuration` goes from 1 to 99: how many turns of its player the battle unit the ability deploys (a summon) lasts. An ability with a `DEPLOY_BATTLE_UNIT` effect must have one, and the others can not.

### `lootableItem/<lootable item id>.toml`

An item a battle unit can loot. Its effects must exist and suit its type: a consumable's effects can not be `WHILE_EQUIPPED`, and an equippable item's effects must all be `WHILE_EQUIPPED`.

Its `price` is what it is worth in gold, from 0 to 999. It is displayed as `10 G` in the inventory.

```toml
name = "Health Potion"
price = 10
type = "CONSUMABLE"              # CONSUMABLE, EQUIPPABLE

[consumable]                     # Only for CONSUMABLE
effectIds = ["health-potion-heal"]

# [equippable]                   # Only for EQUIPPABLE
# effectIds = ["maximum-health-20"]
# equipmentZone = "HEAD"         # HEAD, BODY, ARMS, LEGS, WEAPON
```

### `lootTable/<loot table id>.toml`

The weighted entries a defeated unit may drop. Each entry is a lootable item, which must exist, or nothing. Weights must be greater than 0, and a loot table can not repeat a lootable item nor nothing. The loot table only holds the weights: they are turned into chances when the loot is dropped.

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
name = "Knight"
healthPoints = 100
manaPoints = 30
movementRange = 3
abilities = ["poisoned-sword", "heal"]
```

### `lootDrop/<unit id>.toml`

The loot tables a unit rolls on when one of its battle units is defeated. **The file name is the unit id**, so a unit has at most one loot drop, and a unit without one drops nothing. The unit and the loot tables must exist, and a loot table can not be repeated.

One lootable item, or nothing, is picked at random from the entries of all its loot tables merged together, in proportion to their weights, and dropped on the tile where the battle unit was defeated. It lies there until an inventory stores it, see [Inventory](inventory.md#looting-from-the-battlefield).

```toml
lootTableIds = ["tier-1"]
```

### `player/<player id>.toml`

```toml
name = "Human"
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

### `battleUnit/<battle unit id>.toml`

A battle unit deployed when the game starts. The battle units of the human player explore as a party, the first one leading (see [Exploration](exploration.md)). The battles start when the CPU engages, so there is no file for them.

```toml
unitId = "knight"
playerId = "player-one"
row = 6
column = 6
```

### `npc/<npc id>.toml`

A character standing on a tile of the battlefield, which says its line when the party talks to it. It is no battle unit: it blocks its tile for everyone, nobody can attack it, and no terrain effect applies to it.

```toml
name = "The Guy"
line = "G'day bloke!"
row = 10
column = 2
```

An npc with a `unitId` is recruitable: it joins the party as a battle unit of that unit (e.g.: the viking). The game fails to start when the unit does not exist (`NpcUnitNotFound`).

```toml
name = "Viking"
line = "For Valhalla!"
row = 12
column = 2
unitId = "viking"
```

An npc with a `stock` is a merchant: once the party talks to it, it shows the lootable items it sells, in the order they are listed (e.g.: the merchant). Each item is in unlimited supply, so it is listed once. The game fails to start when an item does not exist (`NpcStockItemNotFound`), when the stock has more than 12 items (`NpcStockTooLarge`), or when it lists an item twice (`NpcStockItemRepeated`).

```toml
name = "Merchant"
line = "How can I help you?"
row = 14
column = 2
stock = ["health-potion", "mana-potion", "hammer"]
```

## How to add a unit

1. Add `resources/unit/<unit id>.toml`.
2. Add its images, following the [asset conventions](assets.md): `unit/<unit id>.png` and `unit/<unit id>_portrait.png`.
3. Optionally, deploy it in the battle with a `resources/battleUnit/<battle unit id>.toml` file.

## How to add an npc

1. Add `resources/npc/<npc id>.toml`, on a tile that can be occupied and where no battle unit is deployed. A merchant only sells [lootable items that exist](#how-to-add-a-lootable-item).
2. Add its images, following the [asset conventions](assets.md): `npc/<npc id>.png` and `npc/<npc id>_portrait.png`. A recruitable npc needs none: it is displayed with the images of its unit, which [is added](#how-to-add-a-unit) instead.

## How to add a lootable item

1. Add the effects it applies, if they do not exist yet: `resources/effect/<effect id>.toml`.
2. Add `resources/lootableItem/<lootable item id>.toml`.
3. Add its image, following the [asset conventions](assets.md): `lootableItem/<lootable item id>.png`.

## How to add a loot table

1. Add the lootable items it drops, if they do not exist yet. See [How to add a lootable item](#how-to-add-a-lootable-item).
2. Add `resources/lootTable/<loot table id>.toml`.

## How to make a unit drop loot

1. Add the loot tables it rolls on, if they do not exist yet. See [How to add a loot table](#how-to-add-a-loot-table).
2. Add `resources/lootDrop/<unit id>.toml`.

Abilities and effects are added the same way, with their TOML file next to their icon. No Kotlin change is needed.
