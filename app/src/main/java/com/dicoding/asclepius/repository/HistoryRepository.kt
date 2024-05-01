package com.dicoding.asclepius.repository

import androidx.lifecycle.LiveData
import com.dicoding.asclepius.database.HistoryResultDao
import com.dicoding.asclepius.entity.HistoryResult

class HistoryRepository(private val historyResultDao: HistoryResultDao) {
    fun getAllHistoryResults(): LiveData<List<HistoryResult>> {
        return historyResultDao.getAllResults()
    }

    fun deleteHistoryResult(result: HistoryResult) {
        historyResultDao.deleteById(result.id)
    }
}
