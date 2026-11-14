package com.mkz.tessavar.party.usecases.commands

import com.mkz.tessavar.battleUnit.domain.BattleUnitEvent.RequestApplyEffect
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto
import com.mkz.tessavar.effect.domain.Effect.Dto.EffectApplicationDto.SourceDto
import com.mkz.tessavar.effect.domain.Effect.Dto.RecipientDto
import com.mkz.tessavar.party.domain.PartyRepository
import com.mkz.tessavar.shared.domain.EventBus

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
                        source = SourceDto.item(lootableItemId),
                        recipient = RecipientDto.battleUnit(battleUnitId),
                        effectId = effectId,
                        destination = null,
                    ),
            ),
        )
    }
}
