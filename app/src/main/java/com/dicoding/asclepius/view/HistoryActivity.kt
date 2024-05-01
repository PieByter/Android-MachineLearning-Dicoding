package com.dicoding.asclepius.view

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.dicoding.asclepius.adapter.HistoryResultAdapter
import com.dicoding.asclepius.database.HistoryDatabase
import com.dicoding.asclepius.repository.HistoryRepository
import com.dicoding.asclepius.viewmodel.HistoryViewModel
import com.dicoding.asclepius.databinding.ActivityHistoryBinding
import com.dicoding.asclepius.viewmodel.HistoryViewModelFactory

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var adapter: HistoryResultAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = HistoryResultAdapter { result ->
            historyViewModel.deleteHistoryResult(result)
        }
        binding.rvHistory.adapter = adapter
        binding.rvHistory.layoutManager = GridLayoutManager(this, 2)

        val database = HistoryDatabase.getDatabase(this)
        val repository = HistoryRepository(database.historyResultDao())
        historyViewModel = ViewModelProvider(
            this,
            HistoryViewModelFactory(repository)
        )[HistoryViewModel::class.java]

        historyViewModel.historyResults.observe(this) { results ->
            if (results.isEmpty()) {
                binding.emptyHistoryTextView.visibility = View.VISIBLE
            } else {
                binding.emptyHistoryTextView.visibility = View.GONE
            }
            adapter.setData(results)
        }
    }
}
