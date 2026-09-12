package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aegiscall.app.data.entity.SpamSignatureEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpamSignatureDao {
    @Query("SELECT * FROM spam_signatures WHERE patternOrNumber = :number LIMIT 1")
    suspend fun findSignature(number: String): SpamSignatureEntity?

    @Query("SELECT * FROM spam_signatures ORDER BY totalReports DESC LIMIT 100")
    fun getTopSpammers(): Flow<List<SpamSignatureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(sig: SpamSignatureEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignatures(sigs: List<SpamSignatureEntity>)

    @Query("SELECT COUNT(*) FROM spam_signatures")
    suspend fun getSignatureCount(): Int
}
