# Game data

The battle is defined by TOML files under [`resources`](../../resources). No game data is written in Kotlin: each subdomain loads its own data, and [`SetupBattle`](../../src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt) only decides the order in which subdomains are initialised.

## How the battle is set up

1. `BattleScene` calls `init()` on each subdomain API. It reads that subdomain's TOML files. Reading resources is asynchronous, so it happens before the battle is set up.
2. `SetupBattle` publishes one `RequestInitialise<Subdomain>` event per subdomain, in this order:

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
| 11 | `RequestInitialiseBattle`        | The battle and its first round | The deployed battle units      |

3. Each `Initialise<Subdomain>` command creates all its data synchronously, so the next subdomain can rely on it. It **MUST NOT** publish per-entity requests instead (e.g.: `RequestAbilityCreation`): those would be handled after every remaining initialisation request.

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
type = "DECREASE_HEALTH"   # DECREASE_HEALTH, INCREASE_HEALTH, TELEPORT, APPLY_EFFECT_ON_NEARBY_ALLIES, DEPLOY_BATTLE_UNIT
damage = 3                 # Only for DECREASE_HEALTH
# healing = 5              # Only for INCREASE_HEALTH
# effectId = "..."         # Only for APPLY_EFFECT_ON_NEARBY_ALLIES
# unitId = "..."           # Only for DEPLOY_BATTLE_UNIT

[application]
type = "ON_TURN_STARTED"   # IMMEDIATELY, ON_TURN_STARTED, BEFORE_APPLYING_EFFECT, ON_DEFEATED
duration = 5               # Only for ON_TURN_STARTED and BEFORE_APPLYING_EFFECT
```

### `ability/<ability id>.toml`

```toml
name = "Skull"
cost = 10
cooldown = 0
targeting = "ADJACENT_ENEMY"   # SELF, ADJACENT_ENEMY, ALL_ADJACENT_ENEMIES, VACANT_TILE_ADJACENT_TO_BATTLE_UNIT, VACANT_TILE_ADJACENT_TO_SELF

[[effects]]
effectId = "low-physical-damage"
target = "SELECTED_TARGET"     # CASTER, SELECTED_TARGET, SELECTED_TILE, CASTER_TILE, NEARBY_ALLIES
```

### `lootableItem/<lootable item id>.toml`

An item a battle unit can loot. Its effects must exist.

```toml
name = "Health Potion"
type = "CONSUMABLE"              # CONSUMABLE, EQUIPPABLE

[consumable]                     # Only for CONSUMABLE
effectIds = ["health-potion-heal"]

# [equippable]                   # Only for EQUIPPABLE
# effectIds = ["..."]
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

One lootable item, or nothing, is picked at random from the entries of all its loot tables merged together, in proportion to their weights, and dropped on the tile where the battle unit was defeated.

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

A battle unit deployed when the battle starts.

```toml
unitId = "knight"
playerId = "player-one"
row = 6
column = 6
```

### `battle/battle.toml`

A single file with the ids of the players, in the order they play each round.

```toml
playerOrder = ["player-one", "player-two"]
```

## How to add a unit

1. Add `resources/unit/<unit id>.toml`.
2. Add its images, following the [asset conventions](assets.md): `unit/<unit id>.png` and `unit/<unit id>_portrait.png`.
3. Optionally, deploy it in the battle with a `resources/battleUnit/<battle unit id>.toml` file.

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
