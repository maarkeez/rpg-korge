package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoMother.battleUnitInfo
import com.mkz.rpg.screen.adapters.presentation.BattleUnitInfoMother.turnActions
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class BattleUnitInfoViewTest : ViewsForTesting() {
    private companion object {
        // More frames than the 150ms slide takes.
        const val FOCUS_ANIMATION_FRAMES = 20
    }

    private val sword =
        AbilityInfoView.AbilityInfo(
            abilityId = "sword",
            manaCost = 0,
            cooldown = 0,
            targeting = AbilityInfoView.AbilityInfo.Targeting.ADJACENT_ENEMY,
            effects = listOf(AbilityInfoView.AbilityInfo.Effect.Damage(10)),
        )

    private fun BattleUnitInfoView.display(
        turnActions: BattleUnitInfoView.TurnActions = turnActions(),
        remainingTurns: Int? = null,
        abilities: List<BattleUnitInfoView.Ability> = emptyList(),
    ) = display(battleUnit = battleUnitInfo(), turnActions = turnActions, remainingTurns = remainingTurns, abilities = abilities)

    @Test
    fun `Init should not be visible when is initialized`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display()
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `BattleUnitInfoView should ring the portrait green when the battle unit is an ally`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display(battleUnit = battleUnitInfo(isAlly = true), turnActions = turnActions(), remainingTurns = null, abilities = emptyList())
            // Then
            battleUnitInfoView.portraitRingColor() shouldBe HudStyle.ALLY
        }

    @Test
    fun `Display should display the remaining steps and casts out of their maximum when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display(turnActions = turnActions(remainingSteps = 2, maximumSteps = 3, remainingCasts = 1, maximumCasts = 1))
            // Then
            battleUnitInfoView.remainingSteps() shouldBe 2
            battleUnitInfoView.maximumSteps() shouldBe 3
            battleUnitInfoView.remainingCasts() shouldBe 1
            battleUnitInfoView.maximumCasts() shouldBe 1
        }

    @Test
    fun `Display should display the steps in white and the casts in gold when is displayed`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display()
            // Then
            battleUnitInfoView.movementPointColors().shouldContainExactly(BattleUnitInfoView.MOVEMENT_POINTS_COLOR)
            battleUnitInfoView.castPointColors().shouldContainExactly(BattleUnitInfoView.CAST_POINTS_COLOR)
        }

    @Test
    fun `Display should display no remaining casts when the battle unit has cast this turn`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display(turnActions = turnActions(remainingCasts = 0, maximumCasts = 1))
            // Then
            battleUnitInfoView.remainingCasts() shouldBe 0
            battleUnitInfoView.maximumCasts() shouldBe 1
        }

    @Test
    fun `Display should only darken the abilities in cooldown when the battle unit has no casts left`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val abilities =
                listOf(
                    BattleUnitInfoView.Ability(abilityId = "sword", cooldownTurnsLeft = 0, hasEnoughMana = true, manaCost = 10),
                    BattleUnitInfoView.Ability(abilityId = "mushroom", cooldownTurnsLeft = 2, hasEnoughMana = true, manaCost = 10),
                    BattleUnitInfoView.Ability(abilityId = "heal", cooldownTurnsLeft = 0, hasEnoughMana = false, manaCost = 10),
                )
            // When
            battleUnitInfoView.display(turnActions = turnActions(remainingCasts = 0, maximumCasts = 1), abilities = abilities)
            // Then
            val (sword, mushroom, heal) = battleUnitInfoView.abilityButtons()
            sword.isAbilityIconDarkened().shouldBeFalse()
            sword.isAbilityIconTintedBlue().shouldBeFalse()
            mushroom.isAbilityIconDarkened().shouldBeTrue()
            mushroom.cooldownBadgeText() shouldBe "2"
            heal.isAbilityIconTintedBlue().shouldBeTrue()
        }

    @Test
    fun `Display should tell how many turns a summon has left, after its name`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            // When
            battleUnitInfoView.display(remainingTurns = 3)
            // Then
            battleUnitInfoView.remainingTurnsText() shouldBe "Vanishes in 3 turns"
            battleUnitInfoView.isRemainingTurnsAfterName().shouldBeTrue()
        }

    @Test
    fun `Display should not tell any turns left when the battle unit never vanishes`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display(remainingTurns = 3)
            // When
            battleUnitInfoView.display(remainingTurns = null)
            // Then
            battleUnitInfoView.remainingTurnsText() shouldBe null
        }

    @Test
    fun `DisplayAbilitySelected should select ability button when ability is selected`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val selectedAbilityIndex = 0
            // When
            battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
            // Then
            val abilityButtons = battleUnitInfoView.abilityButtons()
            val selectedAbilityButton = abilityButtons[selectedAbilityIndex]
            selectedAbilityButton.isSelected().shouldBeTrue()
        }

    @Test
    fun `DisplayAbilitySelected should unselect other ability buttons when ability is selected`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            val abilityButtons = battleUnitInfoView.abilityButtons()
            abilityButtons.forEach { abilityButton -> abilityButton.select() }
            val selectedAbilityIndex = 0
            // When
            battleUnitInfoView.displayAbilitySelected(selectedAbilityIndex)
            // Then
            abilityButtons.forEachIndexed { index, abilityButton ->
                val isSelected = index == selectedAbilityIndex
                abilityButton.isSelected() shouldBe isSelected
            }
        }

    @Test
    fun `BattleUnitInfoView should display the ability info under the ability buttons when an ability is selected`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            // When
            battleUnitInfoView.displayAbilitySelected(0)
            battleUnitInfoView.displayAbilityInfo(sword)
            // Then
            battleUnitInfoView.isAbilityInfoDisplayed().shouldBeTrue()
            battleUnitInfoView.displayedAbilityName() shouldBe "Sword"
            battleUnitInfoView.isAbilityInfoUnderAbilityButtons().shouldBeTrue()
        }

    @Test
    fun `BattleUnitInfoView should hide the ability info when it displays a battle unit`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            battleUnitInfoView.displayAbilityInfo(sword)
            // When
            battleUnitInfoView.display()
            // Then
            battleUnitInfoView.isAbilityInfoDisplayed().shouldBeFalse()
        }

    @Test
    fun `BattleUnitInfoView should hide the ability info when it hides the ability info`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            battleUnitInfoView.displayAbilityInfo(sword)
            // When
            battleUnitInfoView.hideAbilityInfo()
            // Then
            battleUnitInfoView.isAbilityInfoDisplayed().shouldBeFalse()
        }

    @Test
    fun `BattleUnitInfoView should collapse the header when it displays the ability info`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            addChild(battleUnitInfoView)
            battleUnitInfoView.display()
            // When
            battleUnitInfoView.displayAbilityInfo(sword)
            repeat(FOCUS_ANIMATION_FRAMES) { delayFrame() }
            // Then
            battleUnitInfoView.isHeaderCollapsed().shouldBeTrue()
            battleUnitInfoView.areAbilityButtonsUnderHeader().shouldBeTrue()
            battleUnitInfoView.isAbilityInfoUnderAbilityButtons().shouldBeTrue()
        }

    @Test
    fun `BattleUnitInfoView should hide the name, the effects and the bag when it displays the ability info`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display(battleUnit = battleUnitInfo(effectIds = listOf("venom-damage")), turnActions = turnActions(), remainingTurns = null, abilities = emptyList())
            battleUnitInfoView.displayBag(onBagSelected = {})
            // When
            battleUnitInfoView.displayAbilityInfo(sword)
            // Then
            battleUnitInfoView.isNameDisplayed().shouldBeFalse()
            battleUnitInfoView.areEffectsDisplayed().shouldBeFalse()
            battleUnitInfoView.isBagDisplayed().shouldBeFalse()
        }

    @Test
    fun `BattleUnitInfoView should expand the header and show the bag again when it hides the ability info`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            addChild(battleUnitInfoView)
            battleUnitInfoView.display()
            battleUnitInfoView.displayBag(onBagSelected = {})
            battleUnitInfoView.displayAbilityInfo(sword)
            // When
            battleUnitInfoView.hideAbilityInfo()
            repeat(FOCUS_ANIMATION_FRAMES) { delayFrame() }
            // Then
            battleUnitInfoView.isHeaderCollapsed().shouldBeFalse()
            battleUnitInfoView.isNameDisplayed().shouldBeTrue()
            battleUnitInfoView.isBagDisplayed().shouldBeTrue()
            battleUnitInfoView.areAbilityButtonsUnderHeader().shouldBeTrue()
        }

    @Test
    fun `BattleUnitInfoView should expand the header when it displays a battle unit while focused`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            battleUnitInfoView.displayAbilityInfo(sword)
            // When
            battleUnitInfoView.display()
            // Then
            battleUnitInfoView.isHeaderCollapsed().shouldBeFalse()
            battleUnitInfoView.isNameDisplayed().shouldBeTrue()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            // When
            battleUnitInfoView.hide()
            // Then
            battleUnitInfoView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `DisplayBag should display the bag and open the inventory when the bag is clicked`() =
        sharedViewsTest {
            // Given
            var bagSelected = false
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.display()
            addChild(battleUnitInfoView)
            battleUnitInfoView.displayBag(onBagSelected = { bagSelected = true })
            // When
            clickBag(battleUnitInfoView)
            // Then
            battleUnitInfoView.isBagDisplayed().shouldBeTrue()
            bagSelected.shouldBeTrue()
        }

    @Test
    fun `HideBag should not display the bag when the bag is hidden`() =
        sharedViewsTest {
            // Given
            val battleUnitInfoView = BattleUnitInfoView(loadedResourcesLoader(), TextsMother.texts())
            battleUnitInfoView.displayBag(onBagSelected = {})
            // When
            battleUnitInfoView.hideBag()
            // Then
            battleUnitInfoView.isBagDisplayed().shouldBeFalse()
        }
}
