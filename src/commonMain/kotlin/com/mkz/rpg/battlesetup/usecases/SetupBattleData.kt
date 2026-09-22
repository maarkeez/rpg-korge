package com.mkz.rpg.battlesetup.usecases

import com.mkz.rpg.ability.domain.Ability
import com.mkz.rpg.effect.domain.Effect
import com.mkz.rpg.player.domain.Player
import com.mkz.rpg.terrain.domain.Terrain
import com.mkz.rpg.unit.domain.Unit
import kotlinx.serialization.Serializable

@Serializable
data class PlayersFile(
    val player: List<Player.Dto>,
)

@Serializable
data class TerrainsFile(
    val terrain: List<Terrain.Dto>,
)

@Serializable
data class BattlefieldFile(
    val rows: Int,
    val columns: Int,
    val tiles: List<List<String>>,
)

@Serializable
data class EffectsFile(
    val venomDamage: Effect.Dto,
    val lowPhysicalDamage: Effect.Dto,
    val lowDamageHeal: Effect.Dto,
    val teleport: Effect.Dto,
    val venomOnDeath: Effect.Dto,
    val deployBee: Effect.Dto,
) {
    val effects: List<Effect.Dto> = listOf(venomDamage, lowPhysicalDamage, lowDamageHeal, teleport, venomOnDeath, deployBee)
}

@Serializable
data class AbilitiesFile(
    val poisonedSword: Ability.Dto,
    val sword: Ability.Dto,
    val heal: Ability.Dto,
    val mushroom: Ability.Dto,
    val skull: Ability.Dto,
    val teleport: Ability.Dto,
    val bee: Ability.Dto,
) {
    val abilities: List<Ability.Dto> = listOf(poisonedSword, sword, heal, mushroom, skull, teleport, bee)
}

@Serializable
data class UnitsFile(
    val unit: List<Unit.Dto>,
)

@Serializable
data class DeploymentsFile(
    val firstRoundPlayers: List<String>,
    val deployment: List<Deployment>,
)

@Serializable
data class Deployment(
    val battleUnitId: String,
    val unitId: String,
    val playerId: String,
    val deployAtRow: Int,
    val deployAtColumn: Int,
)
