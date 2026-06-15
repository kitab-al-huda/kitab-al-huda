package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.database.dao.ListeningHistoryDao
import com.alfred.kitabalhuda.databinding.ItemHistoryBinding
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

class HistoryAdapter(
    private val onClick: (ListeningHistoryDao.HistoryItem) -> Unit
) : ListAdapter<ListeningHistoryDao.HistoryItem, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale("ar"))
        
        fun bind(item: ListeningHistoryDao.HistoryItem) {
            binding.textSurahName.text = item.sourate.nomArabe
            binding.textReciterName.text = item.reciteur.nom
            binding.textTimestamp.text = dateFormat.format(
                Instant.ofEpochMilli(item.history.timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime()
            )
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListeningHistoryDao.HistoryItem>() {
        override fun areItemsTheSame(oldItem: ListeningHistoryDao.HistoryItem, newItem: ListeningHistoryDao.HistoryItem) = 
            oldItem.history.id == newItem.history.id
        override fun areContentsTheSame(oldItem: ListeningHistoryDao.HistoryItem, newItem: ListeningHistoryDao.HistoryItem) = 
            oldItem == newItem
    }
}
