package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.databinding.ItemPlaylistMiniBinding

class PlaylistMiniAdapter(
    private val onClick: (PlaylistEntity) -> Unit
) : ListAdapter<PlaylistEntity, PlaylistMiniAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlaylistMiniBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemPlaylistMiniBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: PlaylistEntity) {
            binding.textName.text = playlist.name
            binding.root.setOnClickListener { onClick(playlist) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PlaylistEntity>() {
        override fun areItemsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity) = oldItem == newItem
    }
}
