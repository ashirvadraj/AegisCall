package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aegiscall.app.data.entity.SmsCategory
import com.aegiscall.app.data.entity.SmsMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SmsDao {
    @Query("SELECT * FROM sms_messages ORDER BY timestamp DESC")
    fun getAllMessages(): Flow<List<SmsMessageEntity>>

    @Query("SELECT * FROM sms_messages WHERE category = :category ORDER BY timestamp DESC")
    fun getMessagesByCategory(category: SmsCategory): Flow<List<SmsMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SmsMessageEntity): Long

    @Query("DELETE FROM sms_messages WHERE id = :id")
    suspend fun deleteMessage(id: Long)

    @Query("DELETE FROM sms_messages WHERE category = 'OTP_2FA' AND timestamp < :expireBefore")
    suspend fun deleteOldOtps(expireBefore: Long): Int
}
