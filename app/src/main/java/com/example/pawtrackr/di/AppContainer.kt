package com.example.pawtrackr.di

import android.content.Context
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.repository.BusinessConfigRepository
import com.example.pawtrackr.data.repository.CheckoutRepository
import com.example.pawtrackr.data.repository.ClientRepository
import com.example.pawtrackr.data.repository.MessageTemplateRepository
import com.example.pawtrackr.data.repository.PetRepository
import com.example.pawtrackr.data.repository.ServiceRepository
import com.example.pawtrackr.data.repository.SummaryRepository
import com.example.pawtrackr.data.repository.VisitRepository
import com.example.pawtrackr.data.seed.DebugSeeder
import com.pawtrackr.app.core.search.AppSearchEmbeddingEngine
import com.pawtrackr.app.core.runtime.PawtrackrRuntimeServices
import com.pawtrackr.app.core.services.DeviceWorkloadPolicyManager
import com.pawtrackr.app.core.storage.CheckoutTransactionBatchWriter
import com.pawtrackr.app.core.storage.CheckoutTransactionRingBufferAuditBuffer
import com.pawtrackr.app.core.storage.TransactionRingBuffer
import com.pawtrackr.app.features.walkthrough.SharedPreferencesWalkthroughLaunchStore
import com.pawtrackr.app.features.walkthrough.WalkthroughAutoLauncher
import com.pawtrackr.app.features.walkthrough.WalkthroughSessionController

/**
 * Manual dependency container — single source of wiring for the app. (A DI framework
 * like Hilt can replace this later; manual DI keeps the build simple for now and still
 * satisfies the "inject repositories into ViewModels" mandate.)
 */
class AppContainer(context: Context) {
    val database: PawtrackrDatabase = PawtrackrDatabase.getInstance(context)

    /** Local-first, single-user tenant id. Replaced by a real auth uid when sync lands. */
    val currentUserId: String = "local-user"

    val clientRepository: ClientRepository = ClientRepository(database.clientDao())
    val petRepository: PetRepository = PetRepository(database.petDao())
    val serviceRepository: ServiceRepository = ServiceRepository(database.serviceDao())
    val visitRepository: VisitRepository = VisitRepository(database.visitDao())
    val summaryRepository: SummaryRepository = SummaryRepository(database, currentUserId)
    val businessConfigRepository: BusinessConfigRepository = BusinessConfigRepository(database.businessConfigDao())
    val messageTemplateRepository: MessageTemplateRepository = MessageTemplateRepository(database.messageTemplateDao())
    val appSearchEmbeddingEngine: AppSearchEmbeddingEngine = AppSearchEmbeddingEngine()
    val deviceWorkloadPolicyManager: DeviceWorkloadPolicyManager = DeviceWorkloadPolicyManager(context)
    val walkthroughSessionController: WalkthroughSessionController = WalkthroughSessionController()
    val walkthroughLaunchStore = SharedPreferencesWalkthroughLaunchStore(context)
    val walkthroughAutoLauncher = WalkthroughAutoLauncher(walkthroughLaunchStore, walkthroughSessionController)
    val checkoutTransactionRingBuffer = TransactionRingBuffer(
        capacity = 128,
        batchWriter = CheckoutTransactionBatchWriter(database),
        operatingPolicy = deviceWorkloadPolicyManager.operatingPolicy
    )
    val checkoutRepository: CheckoutRepository = CheckoutRepository(
        db = database,
        summaryRepository = summaryRepository,
        checkoutAuditBuffer = CheckoutTransactionRingBufferAuditBuffer(checkoutTransactionRingBuffer)
    )
    val runtimeServices: PawtrackrRuntimeServices = PawtrackrRuntimeServices(
        listOf(
            deviceWorkloadPolicyManager,
            checkoutTransactionRingBuffer
        )
    )
    val debugSeeder: DebugSeeder = DebugSeeder(database, currentUserId)
}
