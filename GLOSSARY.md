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

### Game setup

**Game**:
One whole play, from when it is started new to its victory, which can be saved and loaded again.
_Avoid_: Playthrough, run

**Game setup**:
Getting a game ready to play, new or loaded.
_Avoid_: Battle setup
