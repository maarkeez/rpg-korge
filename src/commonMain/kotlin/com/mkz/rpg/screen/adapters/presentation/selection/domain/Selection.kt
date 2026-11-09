package com.mkz.rpg.screen.adapters.presentation.selection.domain

sealed interface Selection {
    data class NothingSelected(
        private val events: Set<SelectionEvent>,
    ) : Selection {
        companion object {
            fun create() = NothingSelected(setOf(SelectionEvent.Deselected))
        }

        fun deselect() = copy(events = events + SelectionEvent.Deselected)

        fun selectBattleUnit(
            tile: Dto.TileDto,
            battleUnitId: String,
            walkingRange: Set<Dto.TileDto>,
        ) = BattleUnitSelected(
            tile = tile,
            battleUnitId = battleUnitId,
            walkingRange = walkingRange,
            events =
                events +
                    SelectionEvent.BattleUnitSelected(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        walkingRange = walkingRange,
                    ),
        )

        fun pullEvents() = events to copy(events = emptySet())
    }

    data class BattleUnitSelected(
        val tile: Dto.TileDto,
        val battleUnitId: String,
        val walkingRange: Set<Dto.TileDto>,
        private val events: Set<SelectionEvent>,
    ) : Selection {
        fun pullEvents() = events to copy(events = emptySet())

        fun deselect() = NothingSelected(events + SelectionEvent.Deselected)

        fun selectAbility(
            abilityId: String,
            castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        ) = AbilitySelected(
            casterTile = tile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events =
                events +
                    SelectionEvent.AbilitySelected(
                        casterTile = tile,
                        battleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
        )

        fun displayAbilityInfo(abilityId: String) =
            AbilityInfoDisplayed.of(
                casterTile = tile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                events = events,
            )

        fun refreshWalkingRange(
            tile: Dto.TileDto,
            walkingRange: Set<Dto.TileDto>,
        ) = copy(
            tile = tile,
            walkingRange = walkingRange,
            events =
                events +
                    SelectionEvent.BattleUnitSelected(
                        tile = tile,
                        battleUnitId = battleUnitId,
                        walkingRange = walkingRange,
                    ),
        )
    }

    data class AbilitySelected(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val walkingRange: Set<Dto.TileDto>,
        val castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        private val events: Set<SelectionEvent>,
    ) : Selection {
        fun pullEvents() = events to copy(events = emptySet())

        fun selectAbility(
            abilityId: String,
            castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        ) = copy(
            abilityId = abilityId,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events =
                events +
                    SelectionEvent.AbilitySelected(
                        casterTile = casterTile,
                        battleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
        )

        fun displayAbilityInfo(abilityId: String) =
            AbilityInfoDisplayed.of(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                events = events,
            )

        fun deselectAbility() =
            BattleUnitSelected(
                tile = casterTile,
                battleUnitId = battleUnitId,
                walkingRange = walkingRange,
                events =
                    events +
                        SelectionEvent.AbilityDeselected(abilityId) +
                        SelectionEvent.BattleUnitSelected(
                            tile = casterTile,
                            battleUnitId = battleUnitId,
                            walkingRange = walkingRange,
                        ),
            )

        fun previewCastOnCasterOrVacantTile(castGroup: Dto.CastGroupDto) =
            CastPreviewed(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                castGroup = castGroup,
                recipientBattleUnitIds = emptyList(),
                events =
                    events +
                        SelectionEvent.CastOnCasterOrVacantTilePreviewed(
                            casterBattleUnitId = battleUnitId,
                            abilityId = abilityId,
                            castGroup = castGroup,
                        ),
            )

        fun deselect() = NothingSelected(events + SelectionEvent.Deselected)

        fun previewCastOnBattleUnits(
            castGroup: Dto.CastGroupDto,
            recipientBattleUnitIds: List<String>,
        ) = CastPreviewed(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            castGroup = castGroup,
            recipientBattleUnitIds = recipientBattleUnitIds,
            events =
                events +
                    SelectionEvent.CastOnBattleUnitsPreviewed(
                        casterBattleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroup = castGroup,
                        recipientBattleUnitIds = recipientBattleUnitIds,
                    ),
        )
    }

    /**
     * An ability selected only to read what it does: one the battle unit can not cast now (e.g.: in cooldown), or one of
     * a battle unit the human player can not cast with (e.g.: a CPU battle unit). It has no cast range.
     */
    data class AbilityInfoDisplayed(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val walkingRange: Set<Dto.TileDto>,
        private val events: Set<SelectionEvent>,
    ) : Selection {
        companion object {
            fun of(
                casterTile: Dto.TileDto,
                battleUnitId: String,
                abilityId: String,
                walkingRange: Set<Dto.TileDto>,
                events: Set<SelectionEvent>,
            ) = AbilityInfoDisplayed(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                events = events + SelectionEvent.AbilityInfoDisplayed(casterTile = casterTile, battleUnitId = battleUnitId, abilityId = abilityId),
            )
        }

        fun pullEvents() = events to copy(events = emptySet())

        fun deselect() = NothingSelected(events + SelectionEvent.Deselected)

        fun displayAbilityInfo(abilityId: String) =
            of(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                events = events,
            )

        fun selectAbility(
            abilityId: String,
            castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        ) = AbilitySelected(
            casterTile = casterTile,
            battleUnitId = battleUnitId,
            abilityId = abilityId,
            walkingRange = walkingRange,
            castGroupsWhereCanCast = castGroupsWhereCanCast,
            events =
                events +
                    SelectionEvent.AbilitySelected(
                        casterTile = casterTile,
                        battleUnitId = battleUnitId,
                        abilityId = abilityId,
                        castGroupsWhereCanCast = castGroupsWhereCanCast,
                    ),
        )

        // Goes back to the battle unit and its walking range, as deselecting an ability does.
        fun hideAbilityInfo() =
            BattleUnitSelected(
                tile = casterTile,
                battleUnitId = battleUnitId,
                walkingRange = walkingRange,
                events =
                    events +
                        SelectionEvent.AbilityDeselected(abilityId) +
                        SelectionEvent.BattleUnitSelected(
                            tile = casterTile,
                            battleUnitId = battleUnitId,
                            walkingRange = walkingRange,
                        ),
            )
    }

    data class CastPreviewed(
        val casterTile: Dto.TileDto,
        val battleUnitId: String,
        val abilityId: String,
        val walkingRange: Set<Dto.TileDto>,
        val castGroupsWhereCanCast: List<Dto.CastGroupDto>,
        val castGroup: Dto.CastGroupDto,
        // The battle units receiving the cast, by row and column. Empty when the cast does not target any enemy.
        val recipientBattleUnitIds: List<String>,
        private val events: Set<SelectionEvent>,
    ) : Selection {
        fun pullEvents() = events to copy(events = emptySet())

        fun deselect() = NothingSelected(events + SelectionEvent.Deselected)

        /**
         * Keeps the caster selected. Its walking range is displayed once the cast has been resolved, since the cast
         * changes the caster (e.g.: its mana, or its tile when teleported).
         */
        fun confirmCast() =
            BattleUnitSelected(
                tile = casterTile,
                battleUnitId = battleUnitId,
                walkingRange = walkingRange,
                events = events,
            )

        // Goes back to choosing where to cast the ability.
        fun cancelCast() =
            AbilitySelected(
                casterTile = casterTile,
                battleUnitId = battleUnitId,
                abilityId = abilityId,
                walkingRange = walkingRange,
                castGroupsWhereCanCast = castGroupsWhereCanCast,
                events =
                    events +
                        SelectionEvent.AbilitySelected(
                            casterTile = casterTile,
                            battleUnitId = battleUnitId,
                            abilityId = abilityId,
                            castGroupsWhereCanCast = castGroupsWhereCanCast,
                        ),
            )
    }

    interface Dto {
        data class TileDto(
            val row: Int,
            val column: Int,
        )

        data class CastGroupDto(
            val tiles: List<TileDto>,
        )
    }
}
