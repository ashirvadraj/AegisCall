package com.aegiscall.app.data.repository

import com.aegiscall.app.data.dao.CallLogDao
import com.aegiscall.app.data.dao.CallNoteDao
import com.aegiscall.app.data.entity.CallLogEntity
import com.aegiscall.app.data.entity.CallNoteEntity
import kotlinx.coroutines.flow.Flow

class CallRepository(
    private val callLogDao: CallLogDao,
    private val callNoteDao: CallNoteDao
) {
    val allCallLogs: Flow<List<CallLogEntity>> = callLogDao.getAllCallLogs()
    val blockedCalls: Flow<List<CallLogEntity>> = callLogDao.getBlockedCalls()

    suspend fun logCall(callLog: CallLogEntity): Long = callLogDao.insertCallLog(callLog)
    suspend fun deleteCall(id: Long) = callLogDao.deleteCallLog(id)
    suspend fun clearHistory() = callLogDao.clearAll()

    fun getNotesForNumber(phoneNumber: String): Flow<List<CallNoteEntity>> =
        callNoteDao.getNotesForNumber(phoneNumber)

    suspend fun addNote(note: CallNoteEntity): Long = callNoteDao.insertNote(note)
}
