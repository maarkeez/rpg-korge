package com.mkz.rpg.telemetry.usecases.queries

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class SearchConsentTest {
    @Test
    fun `SearchConsent should ask for consent when the game is launched for the first time`() {
        // When
        val isAsked = SearchConsent(FakeTelemetryRepository(null)) { 0.0 }()
        // Then
        isAsked shouldBe true
    }

    @Test
    fun `SearchConsent should not ask for consent once it is given`() {
        // When
        val isAsked = SearchConsent(FakeTelemetryRepository(FakeTelemetryRepository.consented())) { 1_000.0 }()
        // Then
        isAsked shouldBe false
    }
}
