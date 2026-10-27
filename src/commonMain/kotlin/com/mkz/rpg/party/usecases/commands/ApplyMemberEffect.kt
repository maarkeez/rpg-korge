package com.mkz.rpg.party.usecases.commands

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectApplicationDto.ApplicationSourceDto
import com.mkz.rpg.effect.domain.Effect.Dto.EffectTargetDto
import com.mkz.rpg.party.domain.PartyRepository
import com.mkz.rpg.shared.domain.EventBus

/** Applies the effect of an item to a party member, e.g.: a consumed potion. Nothing happens when it is not a party member. */
class ApplyMemberEffect(
    private val partyRepository: PartyRepository,
    private val eventBus: EventBus,
) {
    operator fun invoke(
        battleUnitId: String,
        lootableItemId: String,
        effectId: String,
    ) {
        val party = partyRepository.search() ?: return
        if (battleUnitId !in party.toDto().battleUnitIds) return
        eventBus.publish(
            RequestApplyEffect(
                application =
                    EffectApplicationDto(
                        source = ApplicationSourceDto.item(lootableItemId),
                        target = EffectTargetDto.battleUnit(battleUnitId),
                        effectId = effectId,
                    ),
            ),
        )
    }
}
