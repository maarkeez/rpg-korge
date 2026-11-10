package com.mkz.rpg.battleUnit.usecases.services

class FakeBattleUnitLoader(
    private val deployments: List<BattleUnitLoader.Deployment> = emptyList(),
) : BattleUnitLoader {
    override fun loadDeployments(): List<BattleUnitLoader.Deployment> = deployments
}
