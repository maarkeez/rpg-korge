package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Effect
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Reason
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.AbilityInfo.Targeting
import com.mkz.rpg.screen.adapters.presentation.AbilityInfoView.Mark
import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import kotlin.test.Test

class AbilityInfoViewTest : ViewsForTesting() {
    // The height left under the ability buttons once the header of the battle unit info is collapsed, in the
    // 300-high battle HUD.
    private val heightUnderFocusedHeader = 178.0

    private val skull =
        AbilityInfo(
            abilityId = "skull",
            manaCost = 10,
            cooldown = 0,
            targeting = Targeting.ADJACENT_ENEMY,
            effects = listOf(Effect.Damage(10), Effect.OnDefeatedPoisonAllies(damage = 3, turns = 5)),
        )
    private val mending =
        AbilityInfo(
            abilityId = "mending",
            manaCost = 10,
            cooldown = 2,
            targeting = Targeting.CASTER,
            effects = listOf(Effect.HealSelf(15), Effect.HealAdjacentAllies(15)),
        )

    private suspend fun abilityInfoView() = AbilityInfoView(AbilityInfoView.WIDTH, loadedResourcesLoader(), TextsMother.texts())

    @Test
    fun `AbilityInfoView should display the mana cost and cooldown as pills when it displays an ability`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(mending)
            // Then
            abilityInfoView.isVisibleToUser().shouldBeTrue()
            abilityInfoView.abilityName() shouldBe "Mending"
            abilityInfoView.manaCost() shouldBe "10"
            abilityInfoView.cooldown() shouldBe "2"
            abilityInfoView.targeting() shouldBe "Yourself"
            abilityInfoView.reason().shouldBeNull()
        }

    @Test
    fun `AbilityInfoView should display no cooldown pill when the ability has no cooldown`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(skull)
            // Then
            abilityInfoView.manaCost() shouldBe "10"
            abilityInfoView.cooldown().shouldBeNull()
        }

    @Test
    fun `AbilityInfoView should display one sentence per effect when the ability has several effects`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(skull)
            // Then
            abilityInfoView.effects() shouldBe
                listOf(
                    "Deals 10 damage.",
                    "When defeated: poisons its adjacent allies (3 damage each turn for 5 turns).",
                )
        }

    @Test
    fun `AbilityInfoView should display the numbers of the effects in gold when it displays an ability`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(skull)
            // Then
            abilityInfoView.highlightedWords() shouldBe listOf("10", "(3", "5")
        }

    @Test
    fun `AbilityInfoView should display the reason as a banner when the ability is in cooldown`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(mending.copy(reason = Reason.Cooldown(turnsLeft = 1)))
            // Then
            abilityInfoView.reason() shouldBe "Ready in 1 turn"
            abilityInfoView.isReasonInBanner().shouldBeTrue()
        }

    @Test
    fun `AbilityInfoView should display the mana missing when the ability costs more mana than is left`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(skull.copy(reason = Reason.NotEnoughMana(manaCost = 10, remainingMana = 4)))
            // Then
            abilityInfoView.reason() shouldBe "Needs 10 mana, has 4"
        }

    @Test
    fun `AbilityInfoView should mark the four adjacent tiles as enemies when the ability targets all adjacent enemies`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(skull.copy(abilityId = "cleave", targeting = Targeting.ALL_ADJACENT_ENEMIES))
            // Then
            abilityInfoView.casterMark() shouldBe Mark.CASTER
            abilityInfoView.adjacentMarks() shouldBe List(4) { Mark.ENEMY }
            abilityInfoView.isOneOfTheNeighbours.shouldBeFalse()
        }

    @Test
    fun `AbilityInfoView should fit under the focused header when the ability has three effects and a reason`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            // When
            abilityInfoView.display(
                skull.copy(
                    effects = skull.effects + Effect.DamageEachTurn(damage = 3, turns = 5),
                    reason = Reason.NotEnoughMana(manaCost = 10, remainingMana = 4),
                ),
            )
            // Then
            abilityInfoView.height shouldBeLessThanOrEqual heightUnderFocusedHeader
        }

    @Test
    fun `AbilityInfoView should translate the sentences when the language is Spanish`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = AbilityInfoView(AbilityInfoView.WIDTH, loadedResourcesLoader(), TextsMother.texts("es"))
            // When
            abilityInfoView.display(mending.copy(reason = Reason.Cooldown(turnsLeft = 2)))
            // Then
            abilityInfoView.reason() shouldBe "Disponible en 2 turnos"
            abilityInfoView.effects() shouldBe listOf("Te cura 15 PV.", "Cura 15 PV a los aliados adyacentes.")
        }

    @Test
    fun `AbilityInfoView should not be visible when it is hidden`() =
        sharedViewsTest {
            // Given
            val abilityInfoView = abilityInfoView()
            abilityInfoView.display(skull)
            // When
            abilityInfoView.hide()
            // Then
            abilityInfoView.isVisibleToUser().shouldBeFalse()
        }
}
