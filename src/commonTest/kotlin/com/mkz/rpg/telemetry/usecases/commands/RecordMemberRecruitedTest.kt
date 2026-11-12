package com.mkz.rpg.telemetry.usecases.commands

import com.mkz.rpg.telemetry.domain.FakeTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class RecordMemberRecruitedTest {
    @Test
    fun `RecordMemberRecruited should create a member recruited product event when consent is given`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented())
        // When
        RecordMemberRecruited(RecordProductEvent(telemetryRepository) { 1_000.0 })(unitId = "elf")
        // Then
        val productEvent = telemetryRepository.recorded.shouldHaveSize(1).single()
        productEvent.name shouldBe Name.MEMBER_RECRUITED
        productEvent.occurredAt shouldBe 1_000.0
        productEvent.unitId shouldBe "elf"
    }

    @Test
    fun `RecordMemberRecruited should create nothing when consent is declined`() {
        // Given
        val telemetryRepository = FakeTelemetryRepository(FakeTelemetryRepository.consented().declineConsent(decidedAt = 0.0))
        // When
        RecordMemberRecruited(RecordProductEvent(telemetryRepository) { 1_000.0 })(unitId = "elf")
        // Then
        telemetryRepository.recorded.shouldBeEmpty()
    }
}
