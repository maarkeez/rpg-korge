package battleunit.usecases.services

import org.junit.*

class DistanceServiceTest {
    private val distanceService = DistanceService()

    @Test
    fun `should return manhattan distance when positions are diagonal apart`() {
        // Given
        // When
        val distance = distanceService.manhattanDistance(fromRow = 0, fromColumn = 0, toRow = 3, toColumn = 4)
        // Then
        org.assertj.core.api.Assertions.assertThat(distance).isEqualTo(7)
    }

    @Test
    fun `should return zero when the positions are the same`() {
        // Given
        // When
        val distance = distanceService.manhattanDistance(fromRow = 2, fromColumn = 2, toRow = 2, toColumn = 2)
        // Then
        org.assertj.core.api.Assertions.assertThat(distance).isEqualTo(0)
    }
}
