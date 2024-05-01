package com.dicoding.asclepius.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.asclepius.databinding.ItemHistoryBinding
import com.dicoding.asclepius.entity.HistoryResult
import com.dicoding.asclepius.helper.HistoryResultDiffCallback

class HistoryResultAdapter(private val onDeleteClickListener: (HistoryResult) -> Unit) :
    RecyclerView.Adapter<HistoryResultAdapter.ViewHolder>() {

    private var results: MutableList<HistoryResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHistoryBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = results[position]
        holder.bind(result)
    }

    override fun getItemCount(): Int {
        return results.size
    }

    fun setData(newResults: List<HistoryResult>?) {
        newResults?.let {
            val diffCallback = HistoryResultDiffCallback(results, it)
            val diffResult = DiffUtil.calculateDiff(diffCallback)
            results.clear()
            results.addAll(it)
            diffResult.dispatchUpdatesTo(this)
        }
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(historyResult: HistoryResult) {
            binding.historyResultText.text = historyResult.resultText
            Glide.with(binding.root.context)
                .load(historyResult.imageUri)
                .into(binding.historyResultImage)

            binding.deleteResult.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val clickedResult = results[position]
                    onDeleteClickListener(clickedResult)
                }
            }
        }
    }
}
