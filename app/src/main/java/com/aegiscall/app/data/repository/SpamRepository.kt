package com.aegiscall.app.data.repository

import com.aegiscall.app.data.dao.BlockedNumberDao
import com.aegiscall.app.data.dao.SpamSignatureDao
import com.aegiscall.app.data.entity.BlockedNumberEntity
import com.aegiscall.app.data.entity.SpamSignatureEntity
import kotlinx.coroutines.flow.Flow

class SpamRepository(
    private val blockedNumberDao: BlockedNumberDao,
    private val spamSignatureDao: SpamSignatureDao
) {
    val blockedNumbers: Flow<List<BlockedNumberEntity>> = blockedNumberDao.getAllBlockedNumbers()
    val topSpammers: Flow<List<SpamSignatureEntity>> = spamSignatureDao.getTopSpammers()

    suspend fun blockNumber(numberOrPattern: String, reason: String = "User Blocked"): Long {
        return blockedNumberDao.insertBlockedNumber(
            BlockedNumberEntity(phoneNumberOrPattern = numberOrPattern, reason = reason)
        )
    }

    suspend fun unblockPattern(pattern: String) {
        blockedNumberDao.deleteByPattern(pattern)
    }

    suspend fun isBlocked(phoneNumber: String): Boolean {
        return blockedNumberDao.findMatchingBlockRule(phoneNumber) != null
    }

    suspend fun getSpamSignatureCount(): Int = spamSignatureDao.getSignatureCount()
}
