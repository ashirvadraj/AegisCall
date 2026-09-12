package com.aegiscall.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aegiscall.app.data.dao.*
import com.aegiscall.app.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CallLogEntity::class,
        BlockedNumberEntity::class,
        SpamSignatureEntity::class,
        SmsMessageEntity::class,
        CallNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AegisDatabase : RoomDatabase() {
    abstract fun callLogDao(): CallLogDao
    abstract fun blockedNumberDao(): BlockedNumberDao
    abstract fun spamSignatureDao(): SpamSignatureDao
    abstract fun smsDao(): SmsDao
    abstract fun callNoteDao(): CallNoteDao

    companion object {
        @Volatile
        private var INSTANCE: AegisDatabase? = null

        fun getDatabase(context: Context): AegisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AegisDatabase::class.java,
                    "aegis_call_database"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialSpamSignatures(database.spamSignatureDao(), database.blockedNumberDao())
                    }
                }
            }
        }

        private suspend fun populateInitialSpamSignatures(
            spamDao: SpamSignatureDao,
            blockedDao: BlockedNumberDao
        ) {
            val initialSpamList = listOf(
                SpamSignatureEntity(patternOrNumber = "+18005550199", reportedName = "Global Telemarketing Network", riskLevel = SpamRiskLevel.HIGH_RISK_SPAM, category = "Telemarketing", totalReports = 1420),
                SpamSignatureEntity(patternOrNumber = "+18882345678", reportedName = "Fake Student Loan Relief", riskLevel = SpamRiskLevel.FRAUD_SCAM, category = "Financial Fraud", totalReports = 2890),
                SpamSignatureEntity(patternOrNumber = "+18779991234", reportedName = "Automated Car Warranty Robo", riskLevel = SpamRiskLevel.HIGH_RISK_SPAM, category = "Robocall", totalReports = 5120),
                SpamSignatureEntity(patternOrNumber = "+19001234567", reportedName = "Toll Phishing Bot", riskLevel = SpamRiskLevel.FRAUD_SCAM, category = "Toll Scam", totalReports = 980),
                SpamSignatureEntity(patternOrNumber = "+442079460999", reportedName = "Crypto Investment Boiler Room", riskLevel = SpamRiskLevel.FRAUD_SCAM, category = "Crypto Fraud", totalReports = 3100)
            )
            spamDao.insertSignatures(initialSpamList)

            // Add default wildcard protection
            blockedDao.insertBlockedNumber(
                BlockedNumberEntity(phoneNumberOrPattern = "+1900%", reason = "Block all 1-900 Premium Toll Scams", isWildcard = true)
            )
        }
    }
}
