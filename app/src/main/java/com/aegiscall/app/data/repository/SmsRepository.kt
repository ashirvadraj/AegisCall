package com.aegiscall.app.data.repository

import com.aegiscall.app.data.dao.SmsDao
import com.aegiscall.app.data.entity.SmsCategory
import com.aegiscall.app.data.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

class SmsRepository(private val smsDao: SmsDao) {
    val allMessages: Flow<List<SmsMessageEntity>> = smsDao.getAllMessages()

    fun getMessagesByCategory(category: SmsCategory): Flow<List<SmsMessageEntity>> =
        smsDao.getMessagesByCategory(category)

    suspend fun insertMessage(message: SmsMessageEntity): Long = smsDao.insertMessage(message)
    suspend fun deleteMessage(id: Long) = smsDao.deleteMessage(id)
    suspend fun purgeOldOtps(olderThanMillis: Long): Int = smsDao.deleteOldOtps(olderThanMillis)
}
