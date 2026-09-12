package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aegiscall.app.data.entity.BlockedNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockedNumberDao {
    @Query("SELECT * FROM blocked_numbers ORDER BY addedAt DESC")
    fun getAllBlockedNumbers(): Flow<List<BlockedNumberEntity>>

    @Query("SELECT * FROM blocked_numbers WHERE :number LIKE phoneNumberOrPattern OR phoneNumberOrPattern = :number LIMIT 1")
    suspend fun findMatchingBlockRule(number: String): BlockedNumberEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockedNumber(blockedNumber: BlockedNumberEntity): Long

    @Query("DELETE FROM blocked_numbers WHERE phoneNumberOrPattern = :pattern")
    suspend fun deleteByPattern(pattern: String)

    @Query("UPDATE blocked_numbers SET blockedCallsCount = blockedCallsCount + 1 WHERE id = :id")
    suspend fun incrementBlockedCount(id: Long)
}
