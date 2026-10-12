package com.mkz.rpg.battleUnit.usecases.services

// Loads the given deployments, instead of reading them from the resources.
class FakeBattleUnitLoader(
    private val deployments: List<BattleUnitLoader.Deployment> = emptyList(),
) : BattleUnitLoader {
    override fun loadDeployments(): List<BattleUnitLoader.Deployment> = deployments
}
