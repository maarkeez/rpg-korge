# Plan: First playable demo

> **Status:** done.
>
> **Summary:** The first region of the game, built only from the resources: a lone knight walks out of a corridor, beats
> its first bat, and is told about a village nearby. The village is the hub: a blacksmith, an alchemist, an elder telling
> the story of the Goat Wizard, and a herbalist to recruit. Past the village, a viking waits behind rats and bats, and
> the Goat Wizard waits in its lair, with its rats and bats. Defeating it drops a Bee Essence, which teaches the bee
> summon, and a mystic behind it tells the party to keep exploring. The game does not end: the player can explore again.

## Decisions

| Topic            | Decision                                                                                                                                                                                                       |
|------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Scope            | Only files under `resources` are added or changed. New images are copies of existing ones, no new art. No production Kotlin changes.                                                                         |
| Tests            | The tests that play the real resources (end-to-end and acceptance tests) are moved to the demo: same journeys, new tiles and ids. See [Tests](#tests).                                                         |
| Sandbox content  | Replaced: the battle units, the npcs and the battlefield. The units, abilities, effects and items nobody uses any more stay, as the tests still use some of them (e.g.: the knight with 6 abilities).        |
| Demo units       | New units with their own id, so the sandbox ones the tests use are unchanged: `hero` (named Knight), `viking-warrior` (named Viking) and `goat-wizard` (named Goat Wizard), with copies of the knight, viking and goat images. |
| Map              | One battlefield, 24 columns and 40 rows. The start is in the top-left corner, as the battlefield opens there (see [Map rules](#map-rules)): start, village and lair from north to south.                     |
| Gating           | Only the start corridor forces a fight: the corridor bat blocks it. The recruits are gated by their placement only: the viking stands past the field enemies, the mystic behind the Goat Wizard.              |
| Ending           | None. The mystic congratulates the party, and the enemies respawn, so the player can explore again.                                                                                                           |
| Recovery         | The party never recovers on its own: a fountain in the village and a pond in the meadow (water heals 5 every second standing still), and potions. Mana only comes from potions and elixirs.                 |
| Economy          | Gold only comes from the starting gold and selling loot, for half its price. Fighting the optional enemies buys about one hammer and a few potions before the lair.                                          |
| Bee Essence      | Dropped by every Goat Wizard defeat. The Goat Wizard respawns, so each party member can learn the bee summon, and the extra essences can be sold (60 G).                                                     |

## Map

`K` the knight, `b` bats, `r` rats, `W` the Goat Wizard. Npcs: `F` farmer, `S` blacksmith, `A` alchemist, `E` elder,
`H` herbalist, `V` viking, `M` mystic. Terrain: `.` sand, `#` void, `g` grass, `~` water, `L` lava.

```text
     0         1         2
     012345678901234567890123
  0  ########################
  1  #############...########
  2  .K........b.....########   Start corridor: the corridor bat blocks it
  3  #############...########
  4  ##############.#########
  5  ##############.#########
  6  ########.............###   Meadow: the farmer, a pond, and 3 optional bats in the pockets
  7  ########.......g.....###
  8  ##.....#....F....~~..###
  9  ##.b.............~~..###
 10  ##.....#..g..........###
 11  ##..g..#.............###
 12  ##.b...###...###..g...##
 13  ##.....###...###....b.##
 14  ##########...###########
 15  ##....................##   Village: blacksmith, alchemist, elder, herbalist and a fountain
 16  ##..g...E......H...g..##
 17  ##.........~~.........##
 18  ##.........~~.........##
 19  ##....................##
 20  ##..g..............g..##
 21  ##...S............A...##
 22  ##....................##
 23  ###..###################
 24  ###..###################
 25  ###..###################
 26  ##.........b..........##   Field: 2 rats and 2 bats, the viking past them
 27  ##...........g..r.....##
 28  ##..g.....r.......g.V.##
 29  ##.......g...b........##
 30  ##....................##
 31  ###############..#######
 32  ######............######   Lair: the Goat Wizard, 2 rats and 2 bats, the mystic behind
 33  ######.L..b..b..L.######
 34  ######............######
 35  ######...r....r...######
 36  ######.L...W....L.######
 37  ######............######
 38  ##########.M.###########
 39  ########################
```

- The corridor bat (home `(2,10)`) engages the knight at `(2,5)`: 5 tiles. It wanders up to 3 tiles from home, so it never
  reaches the start, `(2,1)`.
- The village is out of reach of every enemy, wandering included, and a way from the meadow to the village keeps out of
  reach of the optional bats.

### Map rules

Learnt while building the map, and added to [Game data](../architecture/game-data.md#battlefieldbattlefieldtoml):

- Only sand can be next to another terrain, as the transitions are only drawn from sand.
- A tile with void on two sides that meet (a corner) or three sides (a dead end) can not be occupied. A corridor 1 tile
  wide can not turn nor end: it turns in a 3x3 room, and the start corridor ends at the west edge of the battlefield.
- The battlefield opens on its top-left corner and only scrolls once the leader walks, so the party starts within its
  first 8 rows and columns. This is why the start is in the north-west, and the lair in the south.

## The party

| Unit      | Unit id          | Health | Mana | Moves | Abilities             | Gold | Joins                          |
|-----------|------------------|--------|------|-------|-----------------------|------|--------------------------------|
| Knight    | `hero`           | 100    | 30   | 3     | Sword, Heal           | 30   | At the start                   |
| Herbalist | `herbalist`      | 80     | 40   | 3     | Venom Strike, Mending | 10   | In the village                 |
| Viking    | `viking-warrior` | 150    | 30   | 3     | Sword, Cleave         | 20   | In the field, past its enemies |

| Ability      | Cost | Cooldown | Targeting            | Effects                                                          | Icon (copy of)   |
|--------------|------|----------|----------------------|------------------------------------------------------------------|------------------|
| Venom Strike | 5    | 1        | Adjacent enemy       | 5 damage now (`light-physical-damage`), and venom: 3 per turn for 5 turns | `poisoned-sword` |
| Mending      | 10   | 2        | Self                 | Heals 15 to the caster and to every ally next to it (`mending-heal`) | `heal`           |
| Cleave       | 5    | 1        | All adjacent enemies | 15 damage now to each (`heavy-physical-damage`)                  | `sword`          |

## The enemies

| Unit        | Unit id       | Health | Mana | Moves | Abilities                     | Drops                                          |
|-------------|---------------|--------|------|-------|-------------------------------|------------------------------------------------|
| Bat         | `bat`         | 10     | 0    | 5     | Sword                         | `tier-1`: health or mana potion, or nothing    |
| Rat         | `rat`         | 20     | 10   | 3     | Sword, Heal                   | `tier-2`: health or mana potion, a hammer (3 in 28), or nothing |
| Goat Wizard | `goat-wizard` | 150    | 60   | 2     | Sword, Bat, Teleport, Heal    | `goat-wizard`: a Bee Essence, always           |

There are 9 bats (the corridor one, 3 optional in the meadow, 2 in the field and 2 in the lair), 4 rats (2 in the field,
2 in the lair) and the Goat Wizard.

## The npcs

| Npc        | Image (copy of) | Role                               | Line                                                                                                                                       |
|------------|-----------------|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------|
| Farmer     | The Guy         | Points the way to the village      | Thank you for dealing with that bat! My village is just south, past this meadow. Mind the bats nesting off the path.                      |
| Blacksmith | Merchant        | Sells the hammers                  | Fresh from the forge! A sturdy hammer keeps you standing for longer.                                                                      |
| Alchemist  | Merchant        | Sells the potions and essences     | Potions, elixirs, and an essence that bends space. What will it be?                                                                       |
| Elder      | The Guy         | Tells the story of the Goat Wizard | Long ago a goat chewed up a wizard's spellbook and swallowed its magic. The Goat Wizard now rules the lair down south. Beat it, and its bees may answer your call. |
| Herbalist  | The Guy         | Recruitable (`herbalist`)          | Herbs to heal, venom to kill: I know them both. Take me with you, and let's face that goat together!                                       |
| Viking     | Viking          | Recruitable (`viking-warrior`)     | Those beasts kept me pinned here for days! For Valhalla, I fight at your side.                                                            |
| Mystic     | Merchant        | Ends the demo                      | Well done, traveller. This was only the first of many paths: keep exploring this world.                                                   |

The dialog fits about 200 characters, so every line stays under 170.

## The shops

| Seller     | Item             | Price | Effect                                   | Icon (copy of)     |
|------------|------------------|-------|------------------------------------------|--------------------|
| Blacksmith | Hammer           | 50 G  | Weapon: +20 health, +20 mana             | (existing)         |
| Blacksmith | Iron Hammer      | 90 G  | Weapon: +40 health                       | `hammer`           |
| Blacksmith | Rune Hammer      | 150 G | Weapon: +30 health, +40 mana             | `hammer`           |
| Alchemist  | Health Potion    | 10 G  | Heals 10                                 | (existing)         |
| Alchemist  | Mana Potion      | 10 G  | Restores 10 mana                         | (existing)         |
| Alchemist  | Health Elixir    | 25 G  | Heals 30                                 | `health-potion`    |
| Alchemist  | Mana Elixir      | 25 G  | Restores 30 mana                         | `mana-potion`      |
| Alchemist  | Teleport Essence | 100 G | Teaches Teleport                         | (existing)         |
| (drop)     | Bee Essence      | 120 G | Teaches Bee, the summon                  | `teleport-essence` |

## Resources

- `battlefield/battlefield.toml`: the demo map, in the layout `TomlBattlefieldEncoder` writes.
- `battleUnit`: the sandbox battle units are replaced by `hero` and the 14 CPU battle units of the map.
- `unit`: add `hero`, `herbalist`, `viking-warrior` and `goat-wizard`, with their images.
- `ability`: add `venom-strike`, `mending` and `cleave`, with their icons.
- `effect`: add `light-physical-damage`, `heavy-physical-damage`, `mending-heal`, `health-elixir-heal`,
  `mana-elixir-restore`, `maximum-health-30`, `maximum-health-40`, `maximum-mana-40` and `learn-bee`. None stays on the
  battle units, so none needs an icon.
- `lootableItem`: add `iron-hammer`, `rune-hammer`, `health-elixir`, `mana-elixir` and `bee-essence`, with their icons.
- `lootTable`: `tier-2` drops potions, a hammer now and then, or nothing. Add `goat-wizard`, always a Bee Essence.
- `lootDrop`: add `goat-wizard`.
- `npc`: the Guy and the Merchant are replaced by the 7 npcs of the demo. The viking is moved to the field, and recruits
  a `viking-warrior`. The images of the Guy and the Merchant stay, as the view tests display them.

## Tests

The end-to-end and some acceptance tests play the real resources, so they follow the demo. They test the same
journeys:

- End-to-end: the knight starts at `(2,1)` and walks east 4 tiles to `(2,5)`, where the corridor bat engages it and
  walks to `(2,6)`. The party has a single knight, so walking checks that the knight walks, not that a follower
  follows (covered by the party tests). The knight knows Sword and Heal: deploying a bee to block its way is replaced
  by defeating the bat that blocks the corridor, which opens the tiles behind it.
- Acceptance: the knight battle unit is `hero`, at `(2,1)`. Dropping loot uses a Goat Wizard deployed next to the
  knight, as its Bee Essence is always dropped, while the rat loot is random now. The `tier-2` loot table has its new
  entries.

## Known limitations

- Enemies respawn once the party has walked 50 steps, so the corridor bat blocks the corridor again on the way back.
- Losing the knight before recruiting anyone ends the game, as a defeated party member never comes back.
- The potions and the elixirs look alike, and so do the three hammers, until they get their own art.
- The game never ends with a victory: it is only won when every CPU battle unit is defeated at once.
