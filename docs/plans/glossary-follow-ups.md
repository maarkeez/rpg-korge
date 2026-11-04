# Glossary follow-ups

What writing [`GLOSSARY.md`](../../GLOSSARY.md) left to do outside it, as the glossary holds terms only. It grows with each subdomain, until the items are scheduled.

## Ability

### Renames in the code and data

| Today                                                 | Glossary term                               |
|-------------------------------------------------------|---------------------------------------------|
| `cost`                                                | Mana cost                                   |
| `TargetExpression`                                    | Effect target                               |
| `EffectSpec`                                          | Ability effect                              |
| `turnsDuration`                                       | Summon duration                             |
| `reduceCoolDowns`                                     | Cooldown (`reduceCooldowns`)                |
| `SELF`                                                | Caster targeting                            |
| `VACANT_TILE_ADJACENT_TO_SELF`                        | Vacant tile adjacent to the caster          |
| `VACANT_TILE_ADJACENT_TO_BATTLE_UNIT`                 | Vacant tile adjacent to another battle unit |
| `NEARBY_ALLIES`, `APPLY_EFFECT_ON_NEARBY_ALLIES`      | Adjacent allies                             |
| `SELECTED_TARGET`                                     | Targeted battle unit                        |
| `SELECTED_TILE`                                       | Targeted tile                               |

### Player text

- "An empty tile next to any unit" hides that the caster does not count, see Vacant tile adjacent to another battle unit.

### To look at in later sections

- Effect: `EffectTargetDto` is close to the Effect target term, which the rename of `TargetExpression` would collide with.
- Screen: `AttackPreviewView` also previews heals, while "attack" is to be avoided.
- CPU battle brain: `CastKind.ATTACK`, for the same reason.
- Battle unit: Cast, Caster, Known ability, Learned ability and the cooldown turns left are still to be defined.
- Battlefield: the exact meaning of Vacant and Adjacent is still to be defined.

### Open questions

- Cooldown 1 plays the same as cooldown 0: cooldowns go down when a turn of the player starts, and a battle unit has one cast per turn, so the ability is ready again on its next turn either way. It applies to Cleave, Poisoned Sword, Teleport and Venom Strike.
