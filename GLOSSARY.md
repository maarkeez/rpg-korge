# RPG

A turn-based RPG where a party explores a map and fights battles on a grid of tiles.

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
Targeting where the ability is cast on a vacant tile next to the caster.
_Avoid_: Vacant tile adjacent to self, empty tile

**Vacant tile adjacent to another battle unit**:
Targeting where the ability is cast on a vacant tile next to any battle unit other than the caster, ally or enemy.
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
