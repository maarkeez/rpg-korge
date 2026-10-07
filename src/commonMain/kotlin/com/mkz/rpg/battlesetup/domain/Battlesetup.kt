package com.mkz.rpg.battlesetup.domain

@ConsistentCopyVisibility
data class Battlesetup private constructor(
    private val seed: Long?,
    private val battlefield: Battlefield,
    private val deployments: List<Deployment>,
) {
    companion object {
        fun create(battlesetupDto: Dto): Battlesetup =
            Battlesetup(
                seed = battlesetupDto.seed,
                battlefield = Battlefield(battlesetupDto.battlefield.rows, battlesetupDto.battlefield.columns, battlesetupDto.battlefield.tiles),
                deployments = battlesetupDto.deployments.map { Deployment(it.battleUnitId, it.unitId, it.playerId, it.row, it.column) },
            )
    }

    fun toDto(): Dto =
        Dto(
            seed = seed,
            battlefield = Dto.BattlefieldDto(battlefield.rows, battlefield.columns, battlefield.tiles),
            deployments = deployments.map { Dto.DeploymentDto(it.battleUnitId, it.unitId, it.playerId, it.row, it.column) },
        )

    private data class Battlefield(
        val rows: Int,
        val columns: Int,
        val tiles: List<List<String>>,
    )

    private data class Deployment(
        val battleUnitId: String,
        val unitId: String,
        val playerId: String,
        val row: Int,
        val column: Int,
    )

    data class Dto(
        val seed: Long?,
        val battlefield: BattlefieldDto,
        val deployments: List<DeploymentDto>,
    ) {
        data class BattlefieldDto(
            val rows: Int,
            val columns: Int,
            val tiles: List<List<String>>,
        )

        data class DeploymentDto(
            val battleUnitId: String,
            val unitId: String,
            val playerId: String,
            val row: Int,
            val column: Int,
        )
    }
}
