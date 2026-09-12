package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aegiscall.app.data.entity.CachedCallerEntity

@Dao
interface CachedCallerDao {
    @Query("SELECT * FROM cached_callers WHERE phoneNumber = :number LIMIT 1")
    suspend fun getCachedCaller(number: String): CachedCallerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedCaller(caller: CachedCallerEntity)

    @Query("SELECT * FROM cached_callers WHERE phoneNumber LIKE '%' || :query || '%' OR resolvedName LIKE '%' || :query || '%' LIMIT 50")
    suspend fun searchCached(query: String): List<CachedCallerEntity>
}
