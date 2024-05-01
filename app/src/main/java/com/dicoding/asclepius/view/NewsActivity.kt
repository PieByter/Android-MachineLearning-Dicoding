package com.dicoding.asclepius.view

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.asclepius.databinding.ActivityNewsBinding
import com.dicoding.asclepius.adapter.NewsAdapter
import com.dicoding.asclepius.helper.NewsDiffCallback
import com.dicoding.asclepius.viewmodel.NewsViewModel

class NewsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewsBinding
    private lateinit var viewModel: NewsViewModel
    private lateinit var adapter: NewsAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[NewsViewModel::class.java]
        adapter = NewsAdapter(emptyList())

        binding.rvNews.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@NewsActivity.adapter
        }

        viewModel.news.observe(this) { news ->
            if (news != null) {
                news.articles?.let { articles ->
                    val oldList = adapter.newsList
                    val newList = articles.filterNotNull()
                    val diffResult = DiffUtil.calculateDiff(NewsDiffCallback(oldList, newList))
                    adapter.newsList = newList
                    diffResult.dispatchUpdatesTo(adapter)
                    binding.NewsLoading.visibility = View.GONE
                    showToast("News data fetched successfully")
                }
            } else {
                binding.NewsLoading.visibility = View.GONE
                showToast("Failed to fetch news data")
            }
        }

        viewModel.getTopHeadlines("cancer", "health", "en", "91000e343fde480798182dd426423e4d")
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
