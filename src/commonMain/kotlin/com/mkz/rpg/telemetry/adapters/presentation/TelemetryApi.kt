package com.mkz.rpg.telemetry.adapters.presentation

import com.mkz.rpg.shared.domain.EventBus
import com.mkz.rpg.shared.domain.Snapshottable
import com.mkz.rpg.shared.domain.Subscription
import com.mkz.rpg.telemetry.adapters.events.OnBattleFinished
import com.mkz.rpg.telemetry.adapters.events.OnBattleStarted
import com.mkz.rpg.telemetry.adapters.events.OnGameOver
import com.mkz.rpg.telemetry.adapters.events.OnItemBought
import com.mkz.rpg.telemetry.adapters.events.OnItemSold
import com.mkz.rpg.telemetry.adapters.events.OnPartyJoined
import com.mkz.rpg.telemetry.adapters.events.OnQuestAbandoned
import com.mkz.rpg.telemetry.adapters.events.OnQuestCompleted
import com.mkz.rpg.telemetry.adapters.events.OnQuestStarted
import com.mkz.rpg.telemetry.adapters.events.OnRequestRecordGameLoaded
import com.mkz.rpg.telemetry.adapters.events.OnRequestRecordGameSaved
import com.mkz.rpg.telemetry.adapters.events.OnRequestRecordMainMenuOpened
import com.mkz.rpg.telemetry.adapters.events.OnRequestStartGame
import com.mkz.rpg.telemetry.adapters.save.TelemetrySnapshottable
import com.mkz.rpg.telemetry.adapters.storage.FacadeTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.NativeStorageTelemetryRepository
import com.mkz.rpg.telemetry.adapters.storage.ProductEventSender
import com.mkz.rpg.telemetry.adapters.storage.RemoteTelemetryRepository
import com.mkz.rpg.telemetry.domain.TelemetryRepository
import com.mkz.rpg.telemetry.usecases.commands.DeclineConsent
import com.mkz.rpg.telemetry.usecases.commands.GiveConsent
import com.mkz.rpg.telemetry.usecases.commands.LoadTelemetrySnapshot
import com.mkz.rpg.telemetry.usecases.commands.RecordBattleFinished
import com.mkz.rpg.telemetry.usecases.commands.RecordBattleStarted
import com.mkz.rpg.telemetry.usecases.commands.RecordGameLoaded
import com.mkz.rpg.telemetry.usecases.commands.RecordGameOver
import com.mkz.rpg.telemetry.usecases.commands.RecordGameSaved
import com.mkz.rpg.telemetry.usecases.commands.RecordItemBought
import com.mkz.rpg.telemetry.usecases.commands.RecordItemSold
import com.mkz.rpg.telemetry.usecases.commands.RecordMainMenuOpened
import com.mkz.rpg.telemetry.usecases.commands.RecordMemberRecruited
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestAbandoned
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestCompleted
import com.mkz.rpg.telemetry.usecases.commands.RecordQuestStarted
import com.mkz.rpg.telemetry.usecases.commands.StartGame
import com.mkz.rpg.telemetry.usecases.commands.StartSession
import com.mkz.rpg.telemetry.usecases.queries.SearchConsent
import com.mkz.rpg.telemetry.usecases.queries.SearchTelemetrySnapshot
import com.mkz.rpg.telemetry.usecases.services.RecordProductEvent
import korlibs.korge.service.storage.IStorage
import kotlinx.coroutines.CoroutineScope
import kotlin.random.Random

class TelemetryApi(
    storage: IStorage,
    remoteTelemetryRepository: RemoteTelemetryRepository,
    scope: CoroutineScope,
    private val random: Random,
    now: () -> Double,
) {
    private companion object {
        const val ID_BYTES = 16
        const val HEX = 16
        const val BYTE_MASK = 0xff
    }

    private val nativeStorageTelemetryRepository = NativeStorageTelemetryRepository(storage)
    private val telemetryRepository: TelemetryRepository =
        FacadeTelemetryRepository(nativeStorageTelemetryRepository, ProductEventSender(nativeStorageTelemetryRepository, remoteTelemetryRepository, scope))
    private val recordProductEvent = RecordProductEvent(telemetryRepository, now)

    private val startSession = StartSession(telemetryRepository)
    private val recordMainMenuOpened = RecordMainMenuOpened(recordProductEvent)
    private val startGame = StartGame(recordProductEvent, ::newId)
    private val recordGameLoaded = RecordGameLoaded(recordProductEvent, ::newId)
    private val recordGameSaved = RecordGameSaved(recordProductEvent)
    private val recordQuestStarted = RecordQuestStarted(recordProductEvent)
    private val recordQuestCompleted = RecordQuestCompleted(recordProductEvent)
    private val recordQuestAbandoned = RecordQuestAbandoned(recordProductEvent)
    private val recordBattleStarted = RecordBattleStarted(recordProductEvent)
    private val recordBattleFinished = RecordBattleFinished(recordProductEvent)
    private val recordGameOver = RecordGameOver(recordProductEvent)
    private val recordItemBought = RecordItemBought(recordProductEvent)
    private val recordItemSold = RecordItemSold(recordProductEvent)
    private val recordMemberRecruited = RecordMemberRecruited(recordProductEvent)

    val giveConsent = GiveConsent(telemetryRepository, now, ::newId)
    val declineConsent = DeclineConsent(telemetryRepository, now)
    val searchConsent = SearchConsent(telemetryRepository, now)

    val snapshottable: Snapshottable = TelemetrySnapshottable(SearchTelemetrySnapshot(telemetryRepository), LoadTelemetrySnapshot(telemetryRepository))

    init {
        startSession(newId())
    }

    fun listen(eventBus: EventBus): List<Subscription> =
        listOf(
            OnRequestRecordMainMenuOpened(recordMainMenuOpened, eventBus).subscription,
            OnRequestStartGame(startGame, eventBus).subscription,
            OnRequestRecordGameLoaded(recordGameLoaded, eventBus).subscription,
            OnRequestRecordGameSaved(recordGameSaved, eventBus).subscription,
            OnQuestStarted(recordQuestStarted, eventBus).subscription,
            OnQuestCompleted(recordQuestCompleted, eventBus).subscription,
            OnQuestAbandoned(recordQuestAbandoned, eventBus).subscription,
            OnBattleStarted(recordBattleStarted, eventBus).subscription,
            OnBattleFinished(recordBattleFinished, eventBus).subscription,
            OnGameOver(recordGameOver, eventBus).subscription,
            OnItemBought(recordItemBought, eventBus).subscription,
            OnItemSold(recordItemSold, eventBus).subscription,
            OnPartyJoined(recordMemberRecruited, eventBus).subscription,
        )

    private fun newId(): String = random.nextBytes(ID_BYTES).joinToString("") { byte -> (byte.toInt() and BYTE_MASK).toString(HEX).padStart(2, '0') }
}
