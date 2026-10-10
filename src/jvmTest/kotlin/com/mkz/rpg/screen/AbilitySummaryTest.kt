package com.mkz.rpg.screen

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.ability.domain.Ability.Dto.EffectSpecDto
import com.mkz.rpg.ability.domain.Ability.Dto.TargetExpressionDto
import com.mkz.rpg.ability.domain.AbilityMother.ability
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.ApplicationDto.ApplicationTypeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectOutcomeDto.TypeDto
import com.mkz.rpg.effect.usecases.queries.SearchEffectById
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class AbilitySummaryTest {
    private val searchEffectById: SearchEffectById = mock()
    private val abilitySummary = AbilitySummary(searchEffectById)

    private val venomDamage =
        effect(
            id = "venom-damage",
            type = TypeDto.DECREASE_HEALTH,
            application = ApplicationDto(ApplicationTypeDto.ON_TURN_STARTED, ApplicationDto.OnTurnStartedDto(duration = 5), null),
            decreaseHealth = EffectOutcomeDto.DecreaseHealthDto(damage = 3),
        )
    private val physicalDamage =
        effect(id = "low-physical-damage", type = TypeDto.DECREASE_HEALTH, decreaseHealth = EffectOutcomeDto.DecreaseHealthDto(damage = 10))
    private val heal =
        effect(id = "low-damage-heal", type = TypeDto.INCREASE_HEALTH, increaseHealth = EffectOutcomeDto.IncreaseHealthDto(healing = 20))
    private val teleport = effect(id = "teleport", type = TypeDto.TELEPORT)
    private val deployBee =
        effect(id = "deploy-bee", type = TypeDto.DEPLOY_BATTLE_UNIT, deployBattleUnit = EffectOutcomeDto.DeployBattleUnitDto(unitId = "bee"))
    private val venomOnDeath =
        effect(
            id = "venom-on-death",
            type = TypeDto.APPLY_EFFECT_ON_NEARBY_ALLIES,
            application = ApplicationDto(ApplicationTypeDto.ON_DEFEATED, null, null),
            applyEffectOnNearbyAllies = EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto(effectId = "venom-damage"),
        )

    init {
        listOf(venomDamage, physicalDamage, heal, teleport, deployBee, venomOnDeath).forEach { whenever(searchEffectById(it.id)).thenReturn(it) }
    }

    @Test
    fun `should summarize damage over time when the ability is the poisoned sword`() {
        // Given
        val poisonedSword = abilityWith("venom-damage")
        // When
        val summary = abilitySummary(poisonedSword)
        // Then
        assertThat(summary).isEqualTo("3 dmg /turn ×5")
    }

    @Test
    fun `should summarize damage over time when the ability is the mushroom`() {
        // Given
        val mushroom = abilityWith("venom-damage")
        // When
        val summary = abilitySummary(mushroom)
        // Then
        assertThat(summary).isEqualTo("3 dmg /turn ×5")
    }

    @Test
    fun `should summarize damage and the spread on death when the ability is the skull`() {
        // Given
        val skull = abilityWith("low-physical-damage", "venom-on-death")
        // When
        val summary = abilitySummary(skull)
        // Then
        assertThat(summary).isEqualTo("10 dmg, On death: spreads 3 dmg /turn ×5")
    }

    @Test
    fun `should summarize teleport when the ability is teleport`() {
        // Given
        val teleportAbility = abilityWith("teleport")
        // When
        val summary = abilitySummary(teleportAbility)
        // Then
        assertThat(summary).isEqualTo("Teleport")
    }

    @Test
    fun `should summarize the summoned unit when the ability is bee`() {
        // Given
        val beeAbility = abilityWith("deploy-bee")
        // When
        val summary = abilitySummary(beeAbility)
        // Then
        assertThat(summary).isEqualTo("Summon Bee")
    }

    @Test
    fun `should summarize healing when the ability is heal`() {
        // Given
        val healAbility = abilityWith("low-damage-heal")
        // When
        val summary = abilitySummary(healAbility)
        // Then
        assertThat(summary).isEqualTo("+20 HP")
    }

    @Test
    fun `should summarize a question mark when the effect is unknown`() {
        // Given
        val unknownEffectAbility = abilityWith("unknown-effect")
        // When
        val summary = abilitySummary(unknownEffectAbility)
        // Then
        assertThat(summary).isEqualTo("?")
    }

    private fun abilityWith(vararg effectIds: String): Ability.Dto = ability(effectSpecs = effectIds.map { EffectSpecDto(it, TargetExpressionDto(TargetExpressionDto.Type.SELECTED_TARGET)) }).toDto()

    private fun effect(
        id: String,
        type: TypeDto,
        application: ApplicationDto = ApplicationDto(ApplicationTypeDto.IMMEDIATELY, null, null),
        decreaseHealth: EffectOutcomeDto.DecreaseHealthDto? = null,
        increaseHealth: EffectOutcomeDto.IncreaseHealthDto? = null,
        applyEffectOnNearbyAllies: EffectOutcomeDto.ApplyEffectOnNearbyAlliesDto? = null,
        deployBattleUnit: EffectOutcomeDto.DeployBattleUnitDto? = null,
    ) = Effect.Dto(
        id = id,
        outcome = EffectOutcomeDto(type, decreaseHealth, increaseHealth, applyEffectOnNearbyAllies, deployBattleUnit),
        application = application,
    )
}
