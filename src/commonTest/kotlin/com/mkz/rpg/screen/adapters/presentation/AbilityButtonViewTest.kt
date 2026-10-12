package com.mkz.rpg.screen.adapters.presentation

import com.mkz.rpg.screen.adapters.presentation.BattleSceneResourcesLoaderMother.loadedResourcesLoader
import com.mkz.rpg.shared.adapters.presentation.sharedViewsTest
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import korlibs.korge.tests.ViewsForTesting
import korlibs.math.geom.Size
import kotlin.test.Test

class AbilityButtonViewTest : ViewsForTesting() {
    @Test
    fun `Display should be visible when is displayed`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            // When
            abilityButtonView.display(abilityId = "mushroom", canCast = true)
            // Then
            abilityButtonView.isVisibleToUser().shouldBeTrue()
        }

    @Test
    fun `Display should display ability icon when is displayed`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            // When
            abilityButtonView.display(abilityId = "mushroom", canCast = true)
            // Then
            abilityButtonView.isAbilityIconDisplayed().shouldBeTrue()
        }

    @Test
    fun `Display should not darken ability icon when ability can be cast`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            // When
            abilityButtonView.display(abilityId = "mushroom", canCast = true)
            // Then
            abilityButtonView.isAbilityIconDarkened().shouldBeFalse()
        }

    @Test
    fun `Display should darken ability icon when ability can not be cast`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            // When
            abilityButtonView.display(abilityId = "mushroom", canCast = false)
            // Then
            abilityButtonView.isAbilityIconDarkened().shouldBeTrue()
        }

    @Test
    fun `Select should display ability selection icon when is selected`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            // When
            abilityButtonView.select()
            // Then
            abilityButtonView.isSelected().shouldBeTrue()
        }

    @Test
    fun `Select should not display duplicated ability selection icon when is selected twice`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            abilityButtonView.select()
            // When
            abilityButtonView.select()
            // Then
            abilityButtonView.selectionIconCount() shouldBe 1
        }

    @Test
    fun `Unselect should remove ability selection icon when is unselected`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            abilityButtonView.select()
            // When
            abilityButtonView.unselect()
            // Then
            abilityButtonView.isSelected().shouldBeFalse()
        }

    @Test
    fun `Hide should not be visible when is hidden`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            abilityButtonView.display(abilityId = "mushroom", canCast = true)
            // When
            abilityButtonView.hide()
            // Then
            abilityButtonView.isVisibleToUser().shouldBeFalse()
        }

    @Test
    fun `ButtonClick should notify delegate with ability id when button is clicked`() =
        sharedViewsTest {
            // Given
            val abilityButtonView = AbilityButtonView(Size(width = 48.75, height = 48.75), loadedResourcesLoader())
            val selectedAbilityIds = mutableListOf<String>()
            abilityButtonView.setDelegate(
                object : AbilityButtonView.Delegate {
                    override fun abilitySelected(abilityId: String) {
                        selectedAbilityIds += abilityId
                    }
                },
            )
            abilityButtonView.display(abilityId = "mushroom", canCast = true)
            addChild(abilityButtonView)
            // When
            clickAbilityButton(abilityButtonView)
            // Then
            selectedAbilityIds.shouldContainExactly("mushroom")
        }
}
