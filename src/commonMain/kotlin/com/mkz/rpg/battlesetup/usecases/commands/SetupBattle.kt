package com.mkz.rpg.battlesetup.usecases.commands

import com.mkz.rpg.ability.domain.AbilityEvent.RequestAbilityCreation
import com.mkz.rpg.battle.domain.BattleEvent
import com.mkz.rpg.battleUnit.domain.BattleUnitEvent
import com.mkz.rpg.battlefield.domain.BattlefieldEvent
import com.mkz.rpg.battlesetup.usecases.AbilitiesFile
import com.mkz.rpg.battlesetup.usecases.BattlefieldFile
import com.mkz.rpg.battlesetup.usecases.DeploymentsFile
import com.mkz.rpg.battlesetup.usecases.EffectsFile
import com.mkz.rpg.battlesetup.usecases.PlayersFile
import com.mkz.rpg.battlesetup.usecases.TerrainsFile
import com.mkz.rpg.battlesetup.usecases.UnitsFile
import com.mkz.rpg.effect.domain.EffectEvent.RequestEffectCreation
import com.mkz.rpg.player.domain.PlayerEvent
import com.mkz.rpg.shared.adapters.toml.TomlService
import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.terrain.domain.TerrainEvent
import com.mkz.rpg.unit.domain.UnitEvent.RequestUnitCreation
import korlibs.io.file.std.resourcesVfs

class SetupBattle(
    private val eventBus: EventBus,
) {
    private val tomlService = TomlService()

    suspend operator fun invoke() {
        val playersFile = tomlService.deserialize<PlayersFile>(resourcesVfs["setupBattle/players.toml"].readString())
        playersFile.player.forEach { player ->
            eventBus.publish(PlayerEvent.RequestPlayerCreation(player.id, player.name, player.type))
        }

        val terrainsFile = tomlService.deserialize<TerrainsFile>(resourcesVfs["setupBattle/terrains.toml"].readString())
        terrainsFile.terrain.forEach { terrain ->
            eventBus.publish(TerrainEvent.RequestTerrainCreation(terrain))
        }

        val battlefieldFile = tomlService.deserialize<BattlefieldFile>(resourcesVfs["setupBattle/battlefield.toml"].readString())
        eventBus.publish(
            BattlefieldEvent.RequestInitializeBattlefield(
                battlefieldFile.rows,
                battlefieldFile.columns,
                battlefieldFile.tiles,
            ),
        )

        val effectsFile = tomlService.deserialize<EffectsFile>(resourcesVfs["setupBattle/effects.toml"].readString())
        effectsFile.effects.forEach { effect ->
            eventBus.publish(RequestEffectCreation(effect))
        }

        val abilitiesFile = tomlService.deserialize<AbilitiesFile>(resourcesVfs["setupBattle/abilities.toml"].readString())
        abilitiesFile.abilities.forEach { ability ->
            eventBus.publish(RequestAbilityCreation(ability))
        }

        val unitsFile = tomlService.deserialize<UnitsFile>(resourcesVfs["setupBattle/units.toml"].readString())
        unitsFile.unit.forEach { unit ->
            eventBus.publish(RequestUnitCreation(unit))
        }

        val deploymentsFile = tomlService.deserialize<DeploymentsFile>(resourcesVfs["setupBattle/deployments.toml"].readString())
        deploymentsFile.deployment.forEach { deployment ->
            eventBus.publish(
                BattleUnitEvent.RequestDeployBattleUnit(
                    battleUnitId = deployment.battleUnitId,
                    unitId = deployment.unitId,
                    playerId = deployment.playerId,
                    deployAtRow = deployment.deployAtRow,
                    deployAtColumn = deployment.deployAtColumn,
                ),
            )
        }

        eventBus.publish(BattleEvent.RequestStartFirstRound(deploymentsFile.firstRoundPlayers))
    }
}
