package com.mkz.rpg.telemetry.adapters.storage

import com.mkz.rpg.telemetry.adapters.storage.ProductEventMother.productEventRecorded
import com.mkz.rpg.telemetry.domain.TelemetryEvent.ProductEventRecorded.Name
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import korlibs.korge.service.storage.InmemoryStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductEventSenderTest {
    private val storage = InmemoryStorage()
    private val nativeStorageTelemetryRepository = NativeStorageTelemetryRepository(storage)
    private val remoteTelemetryRepository = RecordingRemoteTelemetryRepository()
    private val sender = ProductEventSender(nativeStorageTelemetryRepository, remoteTelemetryRepository, TestScope(UnconfinedTestDispatcher()))

    @Test
    fun `ProductEventSender should send the queue and empty it when the remote accepts it`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.MAIN_MENU_OPENED), productEventRecorded(Name.GAME_STARTED)))
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded(Name.MAIN_MENU_OPENED), productEventRecorded(Name.GAME_STARTED))))
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
        storage.keys().shouldBeEmpty()
    }

    @Test
    fun `ProductEventSender should keep the queue when the remote throws`() {
        // Given
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded()))
        remoteTelemetryRepository.isAccepting = false
        // When
        sender.send()
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(listOf(listOf(productEventRecorded())))
        nativeStorageTelemetryRepository.searchQueued().shouldContainExactly(productEventRecorded())
    }

    @Test
    fun `ProductEventSender should send what was queued while a batch was in flight once it is done`() {
        // Given
        val answer = CompletableDeferred<Unit>()
        remoteTelemetryRepository.answer = answer
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.BATTLE_STARTED)))
        sender.send()
        nativeStorageTelemetryRepository.queue(listOf(productEventRecorded(Name.BATTLE_FINISHED)))
        sender.send()
        // When
        answer.complete(Unit)
        // Then
        remoteTelemetryRepository.batches.shouldContainExactly(
            listOf(productEventRecorded(Name.BATTLE_STARTED)),
            listOf(productEventRecorded(Name.BATTLE_FINISHED)),
        )
        nativeStorageTelemetryRepository.searchQueued().shouldBeEmpty()
    }
}
