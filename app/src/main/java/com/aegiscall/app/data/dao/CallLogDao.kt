package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.aegiscall.app.data.entity.CallLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallLogDao {
    @Query("SELECT * FROM call_logs ORDER BY timestamp DESC")
    fun getAllCallLogs(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE callType = 'BLOCKED_SPAM' ORDER BY timestamp DESC")
    fun getBlockedCalls(): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE phoneNumber LIKE '%' || :query || '%' OR contactName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchCallLogs(query: String): Flow<List<CallLogEntity>>

    @Query("SELECT * FROM call_logs WHERE phoneNumber = :number ORDER BY timestamp DESC")
    suspend fun getCallsForNumber(number: String): List<CallLogEntity>

    @Query("SELECT * FROM call_logs WHERE contactName IS NULL OR contactName = 'Unknown Number' ORDER BY timestamp DESC LIMIT 20")
    fun getRecentUnknownCalls(): Flow<List<CallLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallLog(callLog: CallLogEntity): Long

    @Update
    suspend fun updateCallLog(callLog: CallLogEntity)

    @Query("DELETE FROM call_logs WHERE id = :id")
    suspend fun deleteCallLog(id: Long)

    @Query("DELETE FROM call_logs")
    suspend fun clearAll()
}
