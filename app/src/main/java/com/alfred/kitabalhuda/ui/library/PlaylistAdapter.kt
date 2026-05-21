package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.databinding.ItemPlaylistBinding

class PlaylistAdapter(
    private val onPlaylistClick: (PlaylistEntity) -> Unit,
    private val onPlaylistOptionsClick: (PlaylistEntity) -> Unit
) : ListAdapter<PlaylistEntity, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)
    }

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlist: PlaylistEntity) {
            binding.textPlaylistName.text = playlist.name
            binding.textPlaylistCount.text = binding.root.context.getString(com.alfred.kitabalhuda.R.string.tracks_count, 0)
            
            // TODO: Load cover image if available
            
            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
            
            binding.btnPlaylistOptions.setOnClickListener {
                onPlaylistOptionsClick(playlist)
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<PlaylistEntity>() {
        override fun areItemsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PlaylistEntity, newItem: PlaylistEntity): Boolean {
            return oldItem == newItem
        }
    }
}
