package com.aegiscall.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aegiscall.app.data.entity.CallNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallNoteDao {
    @Query("SELECT * FROM call_notes WHERE phoneNumber = :number ORDER BY createdAt DESC")
    fun getNotesForNumber(number: String): Flow<List<CallNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CallNoteEntity): Long

    @Query("DELETE FROM call_notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}
