package com.mkz.rpg.shared.usecases.acceptance

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AbilityPreviewPurityAcceptanceTest {
    private val battle = ShowcaseBattle()

    @Test
    fun `should leave the battle state and the event bus untouched when every knight ability is previewed on every cast group`() {
        // Given
        val abilityIds = battle.knightAbilityIds()
        require(abilityIds.size == 6)
        val fingerprintBefore = battle.fingerprint()
        var previewCount = 0
        // When
        abilityIds.forEach { abilityId ->
            battle.castGroups(abilityId).forEach { castGroup ->
                battle.battleUnitApi.previewAbilityCast(ShowcaseBattle.KNIGHT, abilityId, battle.positionsOf(castGroup))
                previewCount++
            }
        }
        battle.eventBus.dispatch()
        // Then
        assertThat(previewCount).isGreaterThan(abilityIds.size)
        assertThat(battle.fingerprint()).isEqualTo(fingerprintBefore)
        assertThat(battle.eventBus.events).isEmpty()
    }
}
