package com.example.pawtrackr.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.pawtrackr.data.local.converters.FinancialConverter
import com.example.pawtrackr.data.local.dao.AppFeedbackDao
import com.example.pawtrackr.data.local.dao.BusinessConfigDao
import com.example.pawtrackr.data.local.dao.CategoryDaySummaryDao
import com.example.pawtrackr.data.local.dao.CheckoutTransactionDao
import com.example.pawtrackr.data.local.dao.ClientDao
import com.example.pawtrackr.data.local.dao.ClientInsightSummaryDao
import com.example.pawtrackr.data.local.dao.DaySummaryDao
import com.example.pawtrackr.data.local.dao.DeviceMetadataDao
import com.example.pawtrackr.data.local.dao.DeviceStatusDao
import com.example.pawtrackr.data.local.dao.EmergencyContactDao
import com.example.pawtrackr.data.local.dao.InventoryDao
import com.example.pawtrackr.data.local.dao.MessageTemplateDao
import com.example.pawtrackr.data.local.dao.PetDao
import com.example.pawtrackr.data.local.dao.PresenceRecordDao
import com.example.pawtrackr.data.local.dao.ServiceDao
import com.example.pawtrackr.data.local.dao.ServiceDaySummaryDao
import com.example.pawtrackr.data.local.dao.UserDao
import com.example.pawtrackr.data.local.dao.VisitDao
import com.example.pawtrackr.data.local.entities.AppFeedbackEntity
import com.example.pawtrackr.data.local.entities.BusinessConfigEntity
import com.example.pawtrackr.data.local.entities.CategoryDaySummaryEntity
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import com.example.pawtrackr.data.local.entities.ClientEntity
import com.example.pawtrackr.data.local.entities.ClientInsightSummaryEntity
import com.example.pawtrackr.data.local.entities.DaySummaryEntity
import com.example.pawtrackr.data.local.entities.DeviceMetadataEntity
import com.example.pawtrackr.data.local.entities.DeviceStatusEntity
import com.example.pawtrackr.data.local.entities.EmergencyContactEntity
import com.example.pawtrackr.data.local.entities.InventoryItemEntity
import com.example.pawtrackr.data.local.entities.InventoryTransactionEntity
import com.example.pawtrackr.data.local.entities.MessageTemplateEntity
import com.example.pawtrackr.data.local.entities.PaymentEntity
import com.example.pawtrackr.data.local.entities.PetEntity
import com.example.pawtrackr.data.local.entities.PresenceRecordEntity
import com.example.pawtrackr.data.local.entities.ServiceDaySummaryEntity
import com.example.pawtrackr.data.local.entities.ServiceEntity
import com.example.pawtrackr.data.local.entities.UserEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.data.local.entities.VisitItemEntity

/**
 * The on-device Single Source of Truth. The UI reads only from here; any future
 * sync layer (PowerSync/Supabase) feeds this database rather than the UI directly.
 *
 * Foreign keys honour the iOS SwiftData delete rules (Client→Pet→Visit→VisitItem and
 * Visit→Payment cascade; VisitItem→Service nullify; Client→EmergencyContact and
 * InventoryItem→InventoryTransaction cascade). CheckoutTransaction references
 * visits/pets/clients by loose UUID value (idempotency record), not by FK.
 *
 * All 20 SwiftData @Model types are represented (Inventory contributes two entities).
 */
@Database(
    entities = [
        // Core cluster
        UserEntity::class,
        ClientEntity::class,
        PetEntity::class,
        VisitEntity::class,
        VisitItemEntity::class,
        PaymentEntity::class,
        ServiceEntity::class,
        EmergencyContactEntity::class,
        // Catalog / config
        BusinessConfigEntity::class,
        MessageTemplateEntity::class,
        InventoryItemEntity::class,
        InventoryTransactionEntity::class,
        // Checkout / audit
        CheckoutTransactionEntity::class,
        // Analytics rollups
        DaySummaryEntity::class,
        CategoryDaySummaryEntity::class,
        ServiceDaySummaryEntity::class,
        ClientInsightSummaryEntity::class,
        // Device / sync coordination + feedback
        DeviceMetadataEntity::class,
        DeviceStatusEntity::class,
        PresenceRecordEntity::class,
        AppFeedbackEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(FinancialConverter::class)
abstract class PawtrackrDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun visitDao(): VisitDao
    abstract fun serviceDao(): ServiceDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun businessConfigDao(): BusinessConfigDao
    abstract fun messageTemplateDao(): MessageTemplateDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun checkoutTransactionDao(): CheckoutTransactionDao
    abstract fun daySummaryDao(): DaySummaryDao
    abstract fun categoryDaySummaryDao(): CategoryDaySummaryDao
    abstract fun serviceDaySummaryDao(): ServiceDaySummaryDao
    abstract fun clientInsightSummaryDao(): ClientInsightSummaryDao
    abstract fun deviceMetadataDao(): DeviceMetadataDao
    abstract fun deviceStatusDao(): DeviceStatusDao
    abstract fun presenceRecordDao(): PresenceRecordDao
    abstract fun appFeedbackDao(): AppFeedbackDao

    companion object {
        @Volatile
        private var INSTANCE: PawtrackrDatabase? = null

        fun getInstance(context: Context): PawtrackrDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PawtrackrDatabase::class.java,
                    "pawtrackr_local.db"
                ).build().also { INSTANCE = it }
            }
    }
}
