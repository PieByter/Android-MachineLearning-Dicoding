package com.dicoding.asclepius.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.asclepius.response.NewsResponse
import com.dicoding.asclepius.retrofit.NewsInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class NewsViewModel : ViewModel() {

    private val _news = MutableLiveData<NewsResponse>()
    val news: LiveData<NewsResponse>
        get() = _news

    fun getTopHeadlines(query: String, category: String, language: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NewsInstance.newsService.getTopHeadlines(query, category, language, apiKey)
                }
                if (response.isSuccessful) {
                    _news.value = response.body()
                } else {
                    val errorMessage = "Failed to get top headlines: ${response.code()}"
                    _news.value = NewsResponse(status = "error", message = errorMessage)
                }
            } catch (e: IOException) {
                val errorMessage = "Network error: ${e.message}"
                _news.value = NewsResponse(status = "error", message = errorMessage)
            } catch (e: HttpException) {
                val errorMessage = "HTTP error: ${e.code()}"
                _news.value = NewsResponse(status = "error", message = errorMessage)
            }
        }
    }
}
