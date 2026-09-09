package battlefield.usecases.commands

import battlefield.adapters.storage.InMemoryBattlefieldRepository
import battlefield.domain.BattlefieldEvent
import battlefield.domain.BattlefieldMother
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import shared.domain.FakeEventBus
import shared.domain.assertThat

class InitializeBattlefieldTest {
    private val battlefieldRepository = InMemoryBattlefieldRepository()
    private val eventBus = _root_ide_package_.shared.domain.FakeEventBus()
    private val initializeBattlefield =
        InitializeBattlefield(
            battlefieldRepository = battlefieldRepository,
            eventBus = eventBus,
        )

    @Test
    fun `should create battlefield when no battlefield exists`() {
        // Given
        val rows = 3
        val columns = 4
        val tiles =
            List(rows) {
                List(columns) {
                    _root_ide_package_.battlefield.domain.BattlefieldMother
                        .terrainId()
                }
            }
        // When
        initializeBattlefield(rows = rows, columns = columns, tiles = tiles)
        // Then
        val storedBattlefield = battlefieldRepository.search()?.toDto()
        assertThat(storedBattlefield).isNotNull
        assertThat(storedBattlefield!!.rows).isEqualTo(rows)
        assertThat(storedBattlefield.columns).isEqualTo(columns)
        assertThat(storedBattlefield.tiles).hasSize(rows * columns)
        assertThat(storedBattlefield.tiles.values)
            .allSatisfy { tile -> assertThat(tile.battleUnitId).isNull() }
        _root_ide_package_.shared.domain
            .assertThat(eventBus)
            .hasPublishedEvents(BattlefieldEvent.BattlefieldCreated)
    }

    @Test
    fun `should not create battlefield when a battlefield already exists`() {
        // Given
        val existingBattlefield =
            _root_ide_package_.battlefield.domain.BattlefieldMother
                .battlefield()
        battlefieldRepository.create(existingBattlefield)
        // When
        initializeBattlefield(rows = 3, columns = 3, tiles = emptyList())
        // Then
        assertThat(battlefieldRepository.search()?.toDto())
            .isEqualTo(existingBattlefield.toDto())
        assertThat(eventBus.publishedEvents).isEmpty()
    }
}
