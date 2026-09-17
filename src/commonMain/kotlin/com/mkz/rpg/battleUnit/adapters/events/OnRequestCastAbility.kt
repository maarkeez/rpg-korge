package com.mkz.rpg.battleUnit.adapters.events

import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battleUnit.usecases.commands.CastAbility
import com.mkz.rpg.battleUnit.usecases.queries.WhereCanCast
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.subscribe

class OnRequestCastAbility(
    private val castAbility: CastAbility,
    eventBus: EventBus,
) {
    val subscription =
        eventBus.subscribe<BattleUnitEvent.RequestCastAbility> { event ->
            castAbility(
                battleUnitId = event.battleUnitId,
                abilityId = event.abilityId,
                castGroup =
                    WhereCanCast.CastGroup(
                        positions =
                            event.castGroup.map { position ->
                                WhereCanCast.PositionDto(row = position.row, column = position.column)
                            },
                    ),
            )
        }
}
