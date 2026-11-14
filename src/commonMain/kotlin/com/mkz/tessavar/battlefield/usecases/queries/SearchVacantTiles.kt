package com.mkz.tessavar.battlefield.usecases.queries

import com.mkz.tessavar.battlefield.domain.Battlefield.Dto.PositionDto
import com.mkz.tessavar.battlefield.domain.BattlefieldRepository

class SearchVacantTiles(
    private val isTileVacant: IsTileVacant,
    private val battlefieldRepository: BattlefieldRepository,
) {
    operator fun invoke(
        occupantId: String,
        tilesAround: Int,
    ): List<PositionDto> {
        if (tilesAround <= 0) return emptyList()
        val battlefield = battlefieldRepository.search() ?: return emptyList()
        val position = battlefield.position(occupantId) ?: return emptyList()
        val vacantTiles =
            buildList {
                for (row in position.row - tilesAround..position.row + tilesAround) {
                    for (column in position.column - tilesAround..position.column + tilesAround) {
                        if (isTileVacant(row, column)) {
                            add(PositionDto(row, column))
                        }
                    }
                }
            }
        return vacantTiles
    }
}
