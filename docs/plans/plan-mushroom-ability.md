# Plan - Add mushroom ability

Currently, mushroom ability is a placeholder that deals low physical damage.
Instead, we want the mushroom ability to apply the venom damage effect to all the adjacent enemies when is cast.

# Step 1 - Add missing unit tests

Following our testing guidelines [how-to-add-a-new-test.md](../testing/how-to-add-a-new-test.md) add unit tests for the following classes

- [AbilityButtonView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/AbilityButtonView.kt)
- [AttackPreviewView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/AttackPreviewView.kt)
- [BattlefieldPresenter.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattlefieldPresenter.kt)
- [BattlefieldView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattlefieldView.kt)
- [BattleHudView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattleHudView.kt)
- [BattleInfoPresenter.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattleInfoPresenter.kt)
- [BattleInfoView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattleInfoView.kt)
- [BattleUnitInfoView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattleUnitInfoView.kt)
- [FinishTurnPresenter.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/FinishTurnPresenter.kt)
- [FinishTurnView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/FinishTurnView.kt)
- [HealthBarPreviewView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/HealthBarPreviewView.kt)
- [HealthBarView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/HealthBarView.kt)
- [ManaBarPreviewView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/ManaBarPreviewView.kt)
- [ManaBarView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/ManaBarView.kt)
- [UnitNameView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/UnitNameView.kt)
- [UnitPortraitView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/UnitPortraitView.kt)

# Step 2- Refactor to allow cast group

At the moment abilities can be cast only over a single tile position.

We need to modify our use cases to allow casting an ability to multiple positions at the same time.

As part of this step we will not have an ability that uses this logic, this is a refactor.

We will introduce the concept of "Cast group". From now on

- An ability can be cast in a list of cast groups.
- Each cast group will contain a list of tile positions.
- When an ability is cast over a cast group, the ability will be cast over all the tile positions in that cast group.

We will need to refactor all the layers required to introduce this change

- Views
- Presenters
- Use cases
- Domain

Some of the affected files will be

- [WhereCanCast.kt](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/queries/WhereCanCast.kt)
- [CastAbility.kt](../../src/commonMain/kotlin/com/mkz/rpg/battleUnit/usecases/commands/CastAbility.kt)
- [ProcessAbilitySelected.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/battlefieldHud/usecases/commands/ProcessAbilitySelected.kt)
- [BattlefieldHud.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/battlefieldHud/domain/BattlefieldHud.kt)
- [BattlefieldPresenter.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattlefieldPresenter.kt)
- [BattlefieldView.kt](../../src/commonMain/kotlin/com/mkz/rpg/screen/BattlefieldView.kt)

# Step 3 - Introduce new ability target pattern

We need to introduce a new ability target pattern called `ALL_ADJACENT_ENEMIES`.

An ability with the target pattern `ALL_ADJACENT_ENEMIES` have 1 cast group with all the adjacent enemies (enemies at manhattan distance 1)

# Step 4 - Modify setup battle

Modify setup battle use case [SetupBattle.kt](../../src/commonMain/kotlin/com/mkz/rpg/battlesetup/usecases/commands/SetupBattle.kt) to apply `ALL_ADJACENT_ENEMIES` new target pattern to the `mushroom` ability.
Add a new use case acceptance test following our guidelines [how-to-add-a-new-test.md](../testing/how-to-add-a-new-test.md) to confirm that when the ability is cast, multiple adjacent enemy battle unit have received the effect. 
