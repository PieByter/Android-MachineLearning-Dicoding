package com.dicoding.asclepius.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.repository.HistoryRepository
import com.dicoding.asclepius.entity.HistoryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {
    val historyResults: LiveData<List<HistoryResult>> = repository.getAllHistoryResults()

    fun deleteHistoryResult(result: HistoryResult) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryResult(result)
        }
    }
}

