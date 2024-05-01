package com.dicoding.asclepius.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.asclepius.entity.HistoryResult

@Dao
interface HistoryResultDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertResult(result: HistoryResult)

    @Query("SELECT * FROM HistoryResult ORDER BY id ASC")
    fun getAllResults(): LiveData<List<HistoryResult>>

    @Query("DELETE FROM HistoryResult WHERE id = :id")
    fun deleteById(id: Long)
}
